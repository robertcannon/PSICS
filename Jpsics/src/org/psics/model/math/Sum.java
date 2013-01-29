package org.psics.model.math;

public class Sum extends MultiExpression {


	public double getValue(EvaluationContext ectxt) {
		double ret = 0.;
		for (RealValued e : terms) {
			ret += e.getValue(ectxt);
		}

		return ret;
	}
}
