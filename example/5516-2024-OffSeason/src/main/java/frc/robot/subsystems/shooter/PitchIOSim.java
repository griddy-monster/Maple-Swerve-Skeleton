package frc.robot.subsystems.shooter;

import static frc.robot.constants.PitchConstants.*;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.math.system.plant.DCMotor;
import edu.wpi.first.wpilibj.simulation.SingleJointedArmSim;
import frc.robot.Robot;

public class PitchIOSim implements PitchIO {
    private final SingleJointedArmSim armSim;

    public PitchIOSim() {
        this.armSim = new SingleJointedArmSim(
                DCMotor.getFalcon500(2),
                GEAR_RATIO,
                SingleJointedArmSim.estimateMOI(0.5, 15),
                0.5,
                PITCH_LOWEST_ROTATION_RAD,
                PITCH_HIGHER_LIMIT_RAD,
                true,
                PITCH_LOWEST_ROTATION_RAD);
    }

    @Override
    public void updateInputs(PitchInputs pitchInputs) {
        armSim.update(Robot.defaultPeriodSecs);
        pitchInputs.calibrated = true;
        pitchInputs.pitchSuppliedCurrentAmps = armSim.getCurrentDrawAmps();
        pitchInputs.pitchAngleRad = armSim.getAngleRads();
        pitchInputs.pitchAngularVelocityRadPerSec = armSim.getVelocityRadPerSec();
    }

    @Override
    public void runPitchVoltage(double volts) {
        armSim.setInputVoltage(MathUtil.clamp(volts, -12, 12));
    }
}
