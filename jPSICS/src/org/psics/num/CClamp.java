package org.psics.num;

import org.psics.util.TextDataWriter;


public class CClamp extends Clamp {

	CommandProfile[] profiles;

	CommandProfile activeProfile;

	public CClamp(String id, String at, CommandProfile[] profa, LineStyle style, boolean brec) {
		super(id, at, style, brec);
		profiles = profa;
		activeProfile = profiles[0];
	}


	public void setCommand(int ic) {
		activeProfile = profiles[ic];
	}

	public void advanceControl(double xtime, double dt) {
		double c = activeProfile.valueOver(xtime, dt);
		compartment.setAppliedCurrent(c);
	}


	public double getValue() {
		return compartment.v;
	}


	public void appendTo(TextDataWriter tdw) {
		tdw.add(id);
		tdw.addInts(compartment.getIndex(), 0, (record ? 1 : 0), profiles.length);
		tdw.addMeta("target, type, n profile      (current clamp)");
		tdw.endRow();
		for (CommandProfile cp : profiles) {
			cp.appendTo(tdw);
		}
	}


}
