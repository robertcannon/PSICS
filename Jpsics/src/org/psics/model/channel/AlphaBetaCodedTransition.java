package org.psics.model.channel;

import org.psics.be.AddableTo;
import org.psics.be.BodyValued;
import org.psics.be.Exampled;
import org.psics.codgen.channel.AlphaBetaCodedTransitionEvaluator;
import org.psics.codgen.channel.CodedTransitionEvaluator;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;




@ModelType(standalone=false, usedWithin={KSChannel.class}, tag="Voltaqe dependent transition defined by a fragment of code",
		info="This ")
public class AlphaBetaCodedTransition extends CodedTransition implements Exampled, AddableTo, BodyValued {

	@Label(info = "", tag = "The name of the output variable for the forward transition.")
	public String alphavar;

	@Label(info = "", tag = "The name of the output variable for the reverse transition.")
	public String betavar;



	public void applyMultipliers(double fm, double rm) {
		fwdFactor *= fm;
		revFactor *= rm;
	}



	public void setAlphaVar(String s) {
		alphavar = s;
	}

	public void setBetaVar(String s) {
		betavar = s;
	}



	public String getExampleText() {
		String ret = "<VHalfTransition from=\"C3\" to=\"O\" vHalf=\"-45mV\" z=\"3.5e\"" +
		" gamma=\"0.8\" tau=\"1.2ms\" tauMin=\"0.02ms\"/>";
		return ret;
	}


	public CodedTransitionEvaluator makeEvaluator() {
		CodedTransitionEvaluator ret = null;
		ret = new AlphaBetaCodedTransitionEvaluator(getSysID(), alphavar, betavar, getConstantsArray(), getCodeFragment());
		if (functions != null) {
			for (CodedTransitionFunction tf : functions) {
				tf.addTo(ret);
			}
		}
		return ret;
	}



	public CodedTransition makeLocalCopy() {
		AlphaBetaCodedTransition ret = new AlphaBetaCodedTransition();
		 ret.alphavar = alphavar;
		 ret.betavar = betavar;
		 return ret;
	}





}
