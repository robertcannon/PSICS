package org.psics.model.math;

public class Exp extends UnaryExpression {

	public double getValue(EvaluationContext ectxt) {
		double d = getVal(ectxt);
		double ret = Math.exp(d);
		return ret;
	}

}
