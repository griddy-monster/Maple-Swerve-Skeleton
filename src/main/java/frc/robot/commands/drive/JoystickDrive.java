package frc.robot.commands.drive;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj2.command.Command;
import frc.robot.Constants;
import frc.robot.Robot;
import frc.robot.subsystems.drive.HolonomicDriveSubsystem;
import frc.robot.utils.MapleJoystickDriveInput;
import frc.robot.utils.MechanismControl.MapleSimplePIDController;
import org.littletonrobotics.junction.Logger;

import java.util.function.BooleanSupplier;

import static frc.robot.subsystems.drive.HolonomicDriveSubsystem.isZero;
import static frc.robot.Constants.SwerveDriveChassisConfigs.ChassisRotationalPIDConfigs;

public class JoystickDrive extends Command {
    private final MapleJoystickDriveInput input;
    private final BooleanSupplier useDriverStationCentricSwitch;
    private final HolonomicDriveSubsystem driveSubsystem;
    private final MapleSimplePIDController chassisRotationController;

    private final Timer previousChassisUsageTimer, previousRotationalInputTimer;
    private ChassisSpeeds currentPilotInputSpeeds;
    private Rotation2d currentRotationMaintenanceSetpoint;
    public JoystickDrive(MapleJoystickDriveInput input, BooleanSupplier useDriverStationCentricSwitch, HolonomicDriveSubsystem driveSubsystem) {
        super();
        this.input = input;
        this.useDriverStationCentricSwitch = useDriverStationCentricSwitch;
        this.driveSubsystem = driveSubsystem;
        this.previousChassisUsageTimer = new Timer();
        this.previousChassisUsageTimer.start();
        this.previousRotationalInputTimer = new Timer();
        this.previousRotationalInputTimer.start();

        this.chassisRotationController = new MapleSimplePIDController(new MapleSimplePIDController.SimplePIDConfig(
                driveSubsystem.getChassisMaxAngularVelocity(),
                ChassisRotationalPIDConfigs.ERROR_START_DECELERATE_RADIANS,
                ChassisRotationalPIDConfigs.MINIMUM_CORRECTION_VELOCITY_RAD_PER_SEC,
                ChassisRotationalPIDConfigs.ERROR_TOLERANCE_RADIANS,
                ChassisRotationalPIDConfigs.TIME_LOOK_FORWARD,
                true
        ), driveSubsystem.getFacing().getRadians());

        super.addRequirements(driveSubsystem);
    }

    @Override
    public void initialize() {
        this.previousChassisUsageTimer.reset();
        this.previousRotationalInputTimer.reset();
        this.currentPilotInputSpeeds = new ChassisSpeeds();
        this.currentRotationMaintenanceSetpoint = driveSubsystem.getFacing();
    }

    @Override
    public void execute() {
        final ChassisSpeeds newestPilotInputSpeed = input.getJoystickChassisSpeeds(
                driveSubsystem.getChassisMaxLinearVelocity(), driveSubsystem.getChassisMaxAngularVelocity()
        );
        currentPilotInputSpeeds = driveSubsystem.constrainAcceleration(
                currentPilotInputSpeeds,
                newestPilotInputSpeed,
                Robot.defaultPeriodSecs
        );
        if (!isZero(currentPilotInputSpeeds))
            previousChassisUsageTimer.reset();
        if (Math.abs(currentPilotInputSpeeds.omegaRadiansPerSecond) > 0.05)
            previousRotationalInputTimer.reset();
        Logger.recordOutput("JoystickDrive/current pilot input speeds", currentPilotInputSpeeds.toString());

        if (previousChassisUsageTimer.hasElapsed(Constants.DriveConfigs.nonUsageTimeResetWheels)) {
            driveSubsystem.stop();
            return;
        }

        if (Math.hypot(currentPilotInputSpeeds.vxMetersPerSecond, currentPilotInputSpeeds.vyMetersPerSecond) < 0.01
                && Math.abs(currentPilotInputSpeeds.omegaRadiansPerSecond) < 0.01)
            currentPilotInputSpeeds = new ChassisSpeeds();

        final ChassisSpeeds chassisSpeedsWithRotationMaintenance;
        if (previousRotationalInputTimer.get() > Constants.DriveConfigs.timeActivateRotationMaintenanceAfterNoRotationalInputSeconds) {
            chassisRotationController.setDesiredPosition(currentRotationMaintenanceSetpoint.getRadians());
            chassisSpeedsWithRotationMaintenance = new ChassisSpeeds(
                    currentPilotInputSpeeds.vxMetersPerSecond, currentPilotInputSpeeds.vyMetersPerSecond,
                    chassisRotationController.getMotorPower(driveSubsystem.getMeasuredChassisSpeedsFieldRelative().omegaRadiansPerSecond, driveSubsystem.getFacing().getRadians())
            );
        }
        else {
            chassisSpeedsWithRotationMaintenance = currentPilotInputSpeeds;
            currentRotationMaintenanceSetpoint = driveSubsystem.getFacing();
        }

        Logger.recordOutput("JoystickDrive/rotation maintenance set-point (deg)", currentRotationMaintenanceSetpoint.getDegrees());
        Logger.recordOutput("JoystickDrive/previous rotational input time", previousRotationalInputTimer.get());

        if (useDriverStationCentricSwitch.getAsBoolean())
            driveSubsystem.runDriverStationCentricChassisSpeeds(chassisSpeedsWithRotationMaintenance);
        else
            driveSubsystem.runRobotCentricChassisSpeeds(chassisSpeedsWithRotationMaintenance);
    }
}
