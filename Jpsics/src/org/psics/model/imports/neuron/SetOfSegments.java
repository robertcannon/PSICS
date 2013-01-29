package org.psics.model.imports.neuron;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class SetOfSegments implements AddableTo {

	
	public String name;
	
	
	public ArrayList<NRNSegment> segments = new ArrayList<NRNSegment>();
	
	
	
	public void add(Object obj) {
		if (obj instanceof NRNSegment) {
			segments.add((NRNSegment)obj);
		} else {
			E.typeError(obj);
		}
	}
	
	
	public ArrayList<NRNSegment> getSegments() {
	//	E.info("cell returning segments " + segments);
		return segments;
	}
	
	
}
