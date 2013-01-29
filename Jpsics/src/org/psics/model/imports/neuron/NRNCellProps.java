package org.psics.model.imports.neuron;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class NRNCellProps implements AddableTo {

	public String name;
	
	public ArrayList<mechanism> mechanisms = new ArrayList<mechanism>();
	
	
	
	public void add(Object obj) {
		if (obj instanceof mechanism) {
			mechanisms.add((mechanism)obj);
		} else {
			E.typeError(obj);
		}
	}
	
	
}
