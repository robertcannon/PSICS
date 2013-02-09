package org.psics.model.neuroml;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;


public class NeuroMLMechanism implements AddableTo {

	public String name;
	public String type;

	public  ArrayList<NeuroMLParameter> parameters = new ArrayList<NeuroMLParameter>();



	public void add(Object obj) {
		if (obj instanceof NeuroMLParameter) {
			parameters.add((NeuroMLParameter)obj);
		} else {
			E.error("Cant add " + obj);
		}

	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public ArrayList<NeuroMLParameter> getParameters() {
		return parameters;
	}

}
