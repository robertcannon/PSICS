package org.psics.model.channel;

import org.psics.be.AddableTo;
import org.psics.be.BodyValued;
import org.psics.be.Exampled;
import org.psics.codgen.channel.CodedTransitionEvaluator;
import org.psics.codgen.channel.TauInfCodedTransitionEvaluator;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;




@ModelType(standalone=false, usedWithin={KSChannel.class}, tag="Voltaqe dependent transition defined by a fragment of code",
		info="This ")
public class TauInfCodedTransition extends CodedTransition implements Exampled, AddableTo, BodyValued {

	@Label(info = "", tag = "The name of the output variable for the time constant.")
	public String tauvar;

	@Label(info = "", tag = "The name of the output variable for the equilbrium gate position.")
	public String infvar;



	public void applyMultipliers(double fm, double rm) {
		fwdFactor *= fm;
		revFactor *= rm;
	}



	public void setTauVar(String s) {
		tauvar = s;
	}

	public void setInfVar(String s) {
		infvar = s;
	}



	public String getExampleText() {
		String ret = "<VHalfTransition from=\"C3\" to=\"O\" vHalf=\"-45mV\" z=\"3.5e\"" +
		" gamma=\"0.8\" tau=\"1.2ms\" tauMin=\"0.02ms\"/>";
		return ret;
	}


	public CodedTransitionEvaluator makeEvaluator() {
		CodedTransitionEvaluator ret = null;
		ret = new TauInfCodedTransitionEvaluator(getSysID(), tauvar, infvar, getConstantsArray(), getCodeFragment());
		if (functions != null) {
			for (CodedTransitionFunction tf : functions) {
				tf.addTo(ret);
			}
		}
		return ret;
	}



	public CodedTransition makeLocalCopy() {
		TauInfCodedTransition ret = new TauInfCodedTransition();
		 ret.tauvar = tauvar;
		 ret.infvar = infvar;
		 return ret;
	}





}
