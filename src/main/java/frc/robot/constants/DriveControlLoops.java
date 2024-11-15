package frc.robot.constants;

import static edu.wpi.first.units.Units.MetersPerSecond;
import static edu.wpi.first.units.Units.Volts;
import static frc.robot.constants.DriveTrainConstants.DRIVE_FRICTION_VOLTAGE;

import edu.wpi.first.math.controller.SimpleMotorFeedforward;
import frc.robot.utils.CustomPIDs.MaplePIDController;

public class DriveControlLoops {
    public static final MaplePIDController.MaplePIDConfig CHASSIS_ROTATION_CLOSE_LOOP =
            new MaplePIDController.MaplePIDConfig(
                    Math.toRadians(400), Math.toRadians(90), 0.03, Math.toRadians(3), 0.04, true, 0);

    public static final MaplePIDController.MaplePIDConfig CHASSIS_TRANSLATION_CLOSE_LOOP =
            new MaplePIDController.MaplePIDConfig(2, 1.2, 0, 0.03, 0, false, 0);
    public static final MaplePIDController.MaplePIDConfig STEER_CLOSE_LOOP =
            new MaplePIDController.MaplePIDConfig(0.5, Math.toRadians(90), 0, Math.toRadians(1.5), 0, true, 0);

    public static final SimpleMotorFeedforward DRIVE_OPEN_LOOP = new SimpleMotorFeedforward(
            DRIVE_FRICTION_VOLTAGE.in(Volts), 12 / DriveTrainConstants.CHASSIS_MAX_VELOCITY.in(MetersPerSecond));
    public static final MaplePIDController.MaplePIDConfig DRIVE_CLOSE_LOOP =
            new MaplePIDController.MaplePIDConfig(5, 2, 0, 0, 0, false, 0);
}
