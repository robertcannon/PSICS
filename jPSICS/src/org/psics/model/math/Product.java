package org.psics.model.math;

public class Product extends MultiExpression {



	public double getValue(EvaluationContext ectxt) {
		double ret = 1.;
		for (RealValued e : terms) {
			ret *= e.getValue(ectxt);
		}

		return ret;
	}


}
