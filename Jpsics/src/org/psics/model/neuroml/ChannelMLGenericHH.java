package org.psics.model.neuroml;

import org.psics.be.E;
import org.psics.model.channel.KSTransition;


public class ChannelMLGenericHH extends ChannelMLTransitionRate {


	public String expr;




	public String getCodeLines(String var) {
		String ret = var + " = " + expr + ";";
		for (ChannelMLParameter p : parameters) {
			ret.replaceAll(p.getName(), "" + p.getValue());
		}
		return ret;
	}




	public KSTransition getKSTransition() {
		E.error("no single transition for generic HH");
		return null;
	}



}
