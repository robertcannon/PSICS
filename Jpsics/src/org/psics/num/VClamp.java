package org.psics.num;

import org.psics.util.TextDataWriter;


public class VClamp extends Clamp {

	CommandProfile[] profiles;

	CommandProfile activeProfile;

	public VClamp(String id, String at, CommandProfile[] profa, LineStyle style, boolean brec) {
		super (id, at, style, brec);
		profiles = profa;
	}


	public void setCommand(int ic) {
		activeProfile = profiles[ic];
	}


	public void advanceControl(double xtime, double dt) {
		double v = activeProfile.valueOver(xtime, dt);
		compartment.setClampVoltage(v);
	}



	public double getValue() {
		return compartment.getClampCurrent();
	}


	public void appendTo(TextDataWriter tdw) {
		tdw.add(id);
		tdw.addInts(compartment.getIndex(), 1, (record ? 1 : 0), profiles.length);
		tdw.addMeta("target, type, record(0/1), n profile      (voltage clamp)");
		tdw.endRow();
		for (CommandProfile cp : profiles) {
			cp.appendTo(tdw);
		}
	}

}
