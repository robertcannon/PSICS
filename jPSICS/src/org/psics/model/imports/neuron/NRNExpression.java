package org.psics.model.imports.neuron;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class NRNExpression implements AddableTo {

	public ArrayList<parameter> parameters = new ArrayList<parameter>(); 
	
	
	public String type;
	public String expr;
	public String value;
	
	
	public void add(Object obj) {
		if (obj instanceof parameter) {
			parameters.add((parameter)obj);
		} else {
			E.typeError(obj);
		}
	}
	
}
