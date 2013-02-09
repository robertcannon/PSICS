package org.psics.model.imports.neuron;

public class NRNSegment {


	public String name;
	public String id;
	public String cable;

	public NRNPoint proximal;
	public NRNPoint distal;

	public String parent;

	public NRNProperties properties;


	public String getID() {
		return id;
	}

	public String getParentID() {
		return parent;
	}


	public String getName() {
		return name;
	}

	public NRNPoint getProximal() {
		return proximal;
	}

	public NRNPoint getDistal() {
		return distal;
	}

}
