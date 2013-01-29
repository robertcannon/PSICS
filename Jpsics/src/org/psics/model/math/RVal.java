package org.psics.model.math;

public class RVal {

    String name;
	
	double value;
	
	public RVal(String s, double val) {
		name = s;
		value = val;
	}

	void setValue(double d) {
		value = d;
	}
	
	double getValue() {
		return value;
	}
	
	
}
