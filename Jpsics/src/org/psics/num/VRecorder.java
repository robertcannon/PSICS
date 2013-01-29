package org.psics.num;

import org.psics.util.TextDataWriter;


public class VRecorder extends Accessor {


	public VRecorder(String id, String at, LineStyle style) {
		super (id, at, style, true);

	}

	public double getValue() {
		double ret = 0.;
		if (compartment != null) {
			ret = compartment.v;
		}
		return ret;
	}


	public void appendTo(TextDataWriter tdw) {
		tdw.add(id);
		tdw.addInts(compartment.getIndex(), 1, 0);
		tdw.addMeta("target, type(=1), uu for voltage recorder at " + at);
		tdw.endRow();
	}





}
