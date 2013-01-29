package org.psics.model.neuroml;

import org.psics.be.E;
import org.psics.model.channel.ExpLinearTransition;
import org.psics.model.channel.ExpTransition;
import org.psics.model.channel.KSTransition;
import org.psics.model.channel.SigmoidTransition;


public class ChannelMLParameterizedHH extends ChannelMLTransitionRate {

	public String type;

	public String expr;





	public String getCodeLines(String var) {
		E.info("using expression in parameterized hh to get psics coded equivalent " + expr);
		String ret = var + " = " + expr + ";";
		for (ChannelMLParameter p : parameters) {
			ret.replaceAll(p.getName(), "" + p.getValue());
		}
		return ret;
	}



	private double getParameter(String pnm) {
		double ret = 0.;
		boolean done = false;
		for (ChannelMLParameter cmp : parameters) {
			if (cmp.matches(pnm)) {
				ret = cmp.getValue();
				done = true;
				break;
			}
		}
		if (!done) {
			E.warning("no such parameter " + pnm + " in parameterized HH rate?");
		}
		return ret;
	}


	public KSTransition getKSTransition() {
		KSTransition ret = null;
		double a = getParameter("A");
		double k = getParameter("k");
		double d = getParameter("d");

		if (type.equals("linoid")) {
			ExpLinearTransition wk = new ExpLinearTransition();
			wk.setRate_ms(a);
			wk.setMidpoint_mV(d);
			wk.setScale_mV(1/k);
			ret = wk;

		} else if (type.equals("sigmoid")) {
			SigmoidTransition wk = new SigmoidTransition();
			wk.setRate_ms(a);
			wk.setMidpoint_mV(d);
			wk.setScale_mV( -1/k);   // TODO check the sign convention here
			ret = wk;

		} else if (type.equals("exponential")) {
			ExpTransition wk = new ExpTransition();
			wk.setRate_ms(a);
			wk.setMidpoint_mV(d);
			wk.setScale_mV(1/k);
			ret = wk;

		} else {
			E.error("unrecognized type " + type);
		}
		return ret;
	}


	public boolean isParameterized() {
		return true;
	}

}
