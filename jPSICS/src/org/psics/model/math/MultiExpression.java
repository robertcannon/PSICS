package org.psics.model.math;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;

public abstract class MultiExpression implements RealValued, AddableTo, Assignment {

	public String name;

	ArrayList<RealValued> terms = new ArrayList<RealValued>();

	public void add(Object obj) {
		if (obj instanceof RealValued) {
			terms.add((RealValued)obj);
		} else {
			E.error("cant add " + obj);
		}
	}



	public void apply(EvaluationContext ectxt) {
		ectxt.addDouble(name, getValue(ectxt));
	}


}
