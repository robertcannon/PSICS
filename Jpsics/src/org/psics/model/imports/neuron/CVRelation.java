package org.psics.model.imports.neuron;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class CVRelation implements AddableTo {

		
	public ohmic ohmicRelation;
	
	public void add(Object obj) {
		if (obj instanceof ohmic) {
			ohmicRelation = (ohmic)obj;
		} else {
			E.typeError(obj);
		}
	}

	
	
}
