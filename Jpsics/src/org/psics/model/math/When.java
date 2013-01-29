package org.psics.model.math;

public class When extends UnaryExpression {

	public String test;

	 
	
	boolean applies(EvaluationContext ectxt) {
		return ectxt.getBoolean(test);
	}
	
	public double getValue(EvaluationContext ectxt) {
		return getVal(ectxt);
	}

	
	
}
