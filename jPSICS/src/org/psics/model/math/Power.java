package org.psics.model.math;

import org.psics.be.AddableTo;
import org.psics.be.E;


public class Power implements RealValued, Assignment, AddableTo {

	public String name;
	public Base base;
	public Exponent exponent;


	public void add(Object obj) {
		if (obj instanceof Base) {
			base = (Base)obj;
		} else if (obj instanceof Exponent) {
			exponent = (Exponent)obj;
		} else {
			E.error("cant add " + obj);
		}
	}


	public double getValue(EvaluationContext ectxt) {
		double b = base.getValue(ectxt);
		double p = exponent.getValue(ectxt);
		double ret = Math.pow(b, p);
		return ret;
	}


	public void apply(EvaluationContext ectxt) {
		ectxt.addDouble(name, getValue(ectxt));
	}

}
