package org.psics.model.imports.neuron;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class mechanism implements AddableTo {
	
	public String name;
	
	public ArrayList<property> properties = new ArrayList<property>();
	 
	public ArrayList<parameter> parameters = new ArrayList<parameter>();
	 
	
	public void add(Object obj) {
		if (obj instanceof property) {
			properties.add((property)obj);
	 
		} else if (obj instanceof parameter) {
			parameters.add((parameter)obj);
			
		} else {
			E.typeError(obj);
		}
	}
}
