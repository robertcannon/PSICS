package org.psics.model.channel;

import org.psics.model.math.EvaluationContext;
import org.psics.model.math.UnaryExpression;


public class InfValue extends UnaryExpression {

	public double getValue(EvaluationContext ectxt) {
		return getVal(ectxt);
	}
}
