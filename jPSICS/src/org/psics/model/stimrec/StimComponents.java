package org.psics.model.stimrec;


public class StimComponents {

	public static Class<?>[] stimClasses = {
		Access.class, CurrentClamp.class, VoltageClamp.class, ConductanceClamp.class, VoltageRecorder.class,
		SmartRecorder.class, VoltageProfile.class, VoltagePulse.class, VoltageStep.class, VoltageNoise.class,
		CurrentProfile.class, CurrentPulse.class, CurrentStep.class, CurrentNoise.class,
		ConductanceProfile.class, ConductancePulse.class, ConductanceStep.class, ConductanceNoise.class,
		CellLocation.class, TimeSeries.class
			 };

}
