package org.psics.model.math;

public class Real extends UnaryExpression {

	public double getValue(EvaluationContext ectxt) {
		return getVal(ectxt);
	}
}
