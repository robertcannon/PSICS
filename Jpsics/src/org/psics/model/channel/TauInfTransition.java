package org.psics.model.channel;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.model.Constant;
import org.psics.model.math.Assignment;
import org.psics.model.math.EvaluationContext;
import org.psics.quantity.annotation.ModelType;


@ModelType(standalone = false, usedWithin = { KSChannel.class }, tag = "Voltaqe dependent " +
		"transition defined by functions for Tau and m-infinity", info = "")
public class TauInfTransition extends FunctionTransition implements AddableTo {

	private TauValue p_tauf;
	private InfValue p_inff;


	public void add(Object obj) {

		if (obj instanceof TauValue) {
			p_tauf = (TauValue) obj;

		} else if (obj instanceof InfValue) {
			p_inff = (InfValue) obj;


	    } else if (obj instanceof Constant) {
			super.addConstant((Constant) obj);

		} else if (obj instanceof Assignment) {
			super.addAssignment((Assignment) obj);

		} else {
			E.error("cant add " + obj);
		}
	}


	public void applyMultipliers(double fm, double rm) {
		fwdFactor *= fm;
		revFactor *= rm;

	// 	E.info("applied multipliers " + fwdFactor + " " + revFactor);
	}


	public double[] alphaBeta(double v, double temperature) {

		EvaluationContext ectxt = super.buildContext(v, temperature);
		if (p_tauf == null) {
			E.fatalError("no tau function defined in " + this);
		}
		if (p_inff == null) {
			E.fatalError("no inf function defined in " + this);
		}


		double xtau = p_tauf.getValue(ectxt);
		double xinf = p_inff.getValue(ectxt);

		double alpha = xinf / xtau;
		double beta = (1. - xinf) / xtau;



		double[] ret = { fwdFactor * alpha, revFactor * beta };

	//	if (v < -79.9) {
	//		E.info("evald " + xtau + " " + xinf + " " + alpha + " " + beta + " " + fwdFactor +
	//				" " + revFactor);
	//	}

		return ret;
	}


	public String getExampleText() {
		return "";
	}


	public FunctionTransition makeLocalCopy() {
		TauInfTransition ttr = new TauInfTransition();
		ttr.p_tauf = p_tauf;
		ttr.p_inff = p_inff;
		return ttr;
	}


}
