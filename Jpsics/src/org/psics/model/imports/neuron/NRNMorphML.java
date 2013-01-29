package org.psics.model.imports.neuron;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.Transitional;

public class NRNMorphML implements AddableTo, Transitional {

	public String id;

	public ArrayList<NRNCell> cells = new ArrayList<NRNCell>();



	public void add(Object obj) {
		if (obj instanceof NRNCell) {
			cells.add((NRNCell)obj);
		} else {
			E.typeError(obj);
		}
	}



	public Object getFinal() {
		Object ret = null;
		if (cells.size() > 0) {
			ret = cells.get(0).getCellMorphology(id);
		}
		return ret;
	}



}
