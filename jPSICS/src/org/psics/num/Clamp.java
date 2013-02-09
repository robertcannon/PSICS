package org.psics.num;

import org.psics.util.TextDataWriter;


public abstract class Clamp extends Accessor {

	public Clamp(String sid, String sat, LineStyle ls, boolean brec) {
		super(sid, sat, ls, brec);
	}

	public abstract void advanceControl(double xtime, double dt);

	public abstract void appendTo(TextDataWriter tdw);

	public abstract void setCommand(int ic);

}
