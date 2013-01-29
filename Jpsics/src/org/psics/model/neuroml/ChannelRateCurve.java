package org.psics.model.neuroml;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.model.channel.KSTransition;


public class ChannelRateCurve implements AddableTo {


	ChannelMLTransitionRate rate;



	public void add(Object obj) {
		if (obj instanceof ChannelMLTransitionRate) {
			if (rate == null) {
				rate = (ChannelMLTransitionRate)obj;
			} else {
				E.error("multiple rates?");
			}
		} else {
			E.typeError(obj);
		}
	}



	public KSTransition getKSTransition() {
		return rate.getKSTransition();
	}



	public String getCodeLines(String var) {
		return rate.getCodeLines(var);
	}



	public boolean isParameterized() {
		return rate.isParameterized();
	}


}
