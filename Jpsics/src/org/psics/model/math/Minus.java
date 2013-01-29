package org.psics.model.math;


public class Minus extends UnaryExpression {


	public double getValue(EvaluationContext ectxt) {
		return -1 * getVal(ectxt);
	}

}
