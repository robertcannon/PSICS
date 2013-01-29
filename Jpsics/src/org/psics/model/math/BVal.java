package org.psics.model.math;

public class BVal {
    String name;
	
	boolean value;
	
	public BVal(String s, boolean b) {
		name = s;
		value = b;
	}

	void setValue(boolean b) {
		value = b;
	}
	
	boolean getValue() {
		return value;
	}
	 
	 
}
