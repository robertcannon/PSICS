package org.psics.num;

import org.psics.util.TextDataWriter;


public abstract class Accessor {

	public final static int CURRENT = 0;
	public final static int CONDUCTANCE = 1;


	String id;
	String at;
	LineStyle lineStyle;

	Compartment compartment;

	boolean record;

	public Accessor(String sid, String sat, LineStyle ls, boolean brec) {
		 at = sat;
		 id = sid;
		 lineStyle = ls;
		 record = brec;
	}

	public void setCompartment(Compartment cpt) {
		compartment = cpt;
	}

	public String getAt() {
		return at;
	}

	public String getID() {
		return id;
	}

	public abstract double getValue();

	public abstract void appendTo(TextDataWriter tdw);






}
