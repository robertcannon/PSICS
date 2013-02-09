package org.psics.model.neuroml;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.model.imports.neuron.NRNCell;



public class NeuroMLBiophysics implements AddableTo {
// implements XMLContainer {

	public String units;

	public String xmlContent;

	public void setXMLContent(String s) {
		xmlContent = s;
	}

	public ArrayList<NeuroMLMechanism> mechanisms = new ArrayList<NeuroMLMechanism>();

	public ArrayList<NeuroMLProp> props = new ArrayList<NeuroMLProp>();



	public void add(Object obj) {
		if (obj instanceof NeuroMLMechanism) {
			mechanisms.add((NeuroMLMechanism)obj);

		} else if (obj instanceof NeuroMLProp) {
			props.add((NeuroMLProp)obj);

		} else if (obj instanceof MorphMLCell || obj instanceof NRNCell) {
			// this is just some craziness in certain files where the biophysics contains a
			// cell properties declaration using the same element name (cell) as for a proper
			// cell declaration

		} else {
			E.error("cant add " + obj);
		}
	}

	public ArrayList<NeuroMLMechanism> getMechanisms() {
		return mechanisms;
	}

	public ArrayList<NeuroMLProp> getProperties() {
		return props;
	}

}
