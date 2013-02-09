package org.psics.model.math;

public class Reciprocol extends UnaryExpression {

	 
	public double getValue(EvaluationContext ectxt) {
		return 1. / getVal(ectxt);
	}

}
