package org.psics.model.imports.neuron;

import java.util.ArrayList;

import org.psics.be.AddableTo;

public class MembraneProperties implements AddableTo {
	
	

	public ArrayList<section> sections = new ArrayList<section>();
	
	
	
	public void add(Object obj) {
		sections.add((section)obj);
	}
	
	
}
