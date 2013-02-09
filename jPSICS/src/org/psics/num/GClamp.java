package org.psics.num;

import org.psics.be.E;
import org.psics.util.TextDataWriter;


public class GClamp extends Clamp {

	CommandProfile[] profiles;

	CommandProfile activeProfile;

	double potential;

	public GClamp(String id, String at, double vto, CommandProfile[] profa, LineStyle style, boolean brec) {
		super(id, at, style, brec);
		potential = vto;
		profiles = profa;
		activeProfile = profiles[0];
	}


	public void setCommand(int ic) {
		activeProfile = profiles[ic];
	}

	public void advanceControl(double xtime, double dt) {
		// double g = activeProfile.valueOver(xtime, dt);
		E.missing("gclamp stuff");
		//	compartment.setAppliedCurrent(c);
	}


	public double getValue() {
		E.missing("g clamp ");
		return 0.; // compartment.v;
	}


	public void appendTo(TextDataWriter tdw) {
		tdw.add(id);
		tdw.addInts(compartment.getIndex(), 2, (record ? 1 : 0), profiles.length);
		tdw.addMeta("target, type, n profile      (conductance clamp)");
		tdw.endRow();
		tdw.add(potential);
		tdw.addMeta("Potential");
		tdw.endRow();
		for (CommandProfile cp : profiles) {
			cp.appendTo(tdw);
		}
	}


}
