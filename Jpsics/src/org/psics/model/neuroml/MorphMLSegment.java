package org.psics.model.neuroml;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class MorphMLSegment implements AddableTo {


	public String name;
	public String id;
	public String cable;

	public MorphMLPoint proximal;
	public MorphMLPoint distal;

	public String parent;

	public MorphMLProperties properties; // sometimes find this in Neuron output


	public void add(Object obj) {
		if (obj instanceof MorphMLProperties) {
			// TODO - do we need these?
		} else {
			E.warning("cant add " + obj);
		}
	}


	public String getID() {
		return id;
	}

	public String getParentID() {
		return parent;
	}


	public String getName() {
		return name;
	}

	public MorphMLPoint getProximal() {
		return proximal;
	}

	public MorphMLPoint getDistal() {
		return distal;
	}

}
