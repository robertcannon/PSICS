package org.psics.model.neuroml;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.model.channel.KSTransition;


public abstract class ChannelMLTransitionRate implements AddableTo {

	public abstract KSTransition getKSTransition();

	public abstract String getCodeLines(String var);


	public ArrayList<ChannelMLParameter> parameters = new ArrayList<ChannelMLParameter>();



	public void add(Object obj) {
		if (obj instanceof ChannelMLParameter) {
			parameters.add((ChannelMLParameter)obj);
		} else {
			E.typeError(obj);
		}
	}

	public boolean isParameterized() {
		return false;
	}


}
