package org.psics.model.math;

import org.psics.be.AddableTo;
import org.psics.be.E;

public abstract class UnaryExpression implements RealValued, AddableTo, Assignment {

	public String name;

	RealValued expression;



	public String getName() {
		return name;
	}


	public void add(Object obj) {
		if (obj instanceof RealValued) {
			if (expression == null) {
				expression = (RealValued)obj;
			} else {
				E.error("only one expression allowed in a unary expression " + this);
			}
		} else {
			E.error("cant add " + obj);
		}
	}



	public double getVal(EvaluationContext ectxt) {
		double ret = 0.;
		if (expression != null) {
			ret = expression.getValue(ectxt);
		}
		return ret;
	}


	// this is the default, but can be overriden by functions like abs, sin, etc
	public abstract double getValue(EvaluationContext ectxt);


	public void apply(EvaluationContext ectxt) {
		ectxt.addDouble(name, getValue(ectxt));
	}







}
