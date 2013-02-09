package org.psics.model.math;

public class Abs extends UnaryExpression {


	public double getValue(EvaluationContext ectxt) {
		double d = getVal(ectxt);
		if (d < 0.) {
			 d = -d;
		}
		return d;
	}


}
