package org.psics.model.imports.neuron;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class section implements AddableTo {

	public String id;

	public String name;

	public ArrayList<property> properties = new ArrayList<property>();

	public ArrayList<mechanism> mechanisms = new ArrayList<mechanism>();


	public void add(Object obj) {
		if (obj instanceof property) {
			properties.add((property)obj);
		} else if (obj instanceof mechanism) {
			mechanisms.add((mechanism)obj);
		} else if (obj instanceof section) {
			// TODO - just ignored for now!

		} else {
			E.typeError(obj);
		}
	}



}
