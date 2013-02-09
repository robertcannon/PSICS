package org.psics.model.imports.neuron;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class SetOfCables implements AddableTo {


	public ArrayList<NRNCable> cables = new ArrayList<NRNCable>();



	public void add(Object obj) {
		if (obj instanceof NRNCable) {
			cables.add((NRNCable)obj);
		} else {
			E.typeError(obj);
		}
	}




}
