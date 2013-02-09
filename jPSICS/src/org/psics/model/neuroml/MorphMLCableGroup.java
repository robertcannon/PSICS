package org.psics.model.neuroml;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;


public class MorphMLCableGroup implements AddableTo {

	public String name;


	public ArrayList<MorphMLCable> cables = new ArrayList<MorphMLCable>();


	public void add(Object obj) {
		if (obj instanceof MorphMLCable) {
			cables.add((MorphMLCable)obj);

		} else {
			E.warning("cant add " + obj);
		}
	}


	public String getName() {
		return name;
	}

}
