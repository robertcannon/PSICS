package org.psics.model.math;

import org.psics.be.AddableTo;
import org.psics.be.E;

public abstract class UnaryBooleanExpression implements BooleanValued, AddableTo, Assignment {

	public String name;

	BooleanValued expression;


	public void add(Object obj) {
		if (obj instanceof BooleanValued) {
			if (expression == null) {
				expression = (BooleanValued)obj;
			} else {
				E.error("only one expression allowed in a Real");
			}
		} else {
			E.error("cant add " + obj);
		}
	}

	boolean getBVal(EvaluationContext ectxt) {
		boolean ret = false;
		if (expression != null) {
			ret = expression.getBValue(ectxt);
		}
		return ret;
	}



	public boolean getBValue(EvaluationContext ectxt) {
		return getBVal(ectxt);
	}


	public void apply(EvaluationContext ectxt) {
		ectxt.addBoolean(name, getBVal(ectxt));
	}

}
