package org.psics.model.math;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class Condition implements  BooleanValued, Assignment, AddableTo {

	public String name;

	public Lesser lesser;
	public Greater greater;


	public void add(Object obj) {
		if (obj instanceof Lesser) {
			lesser = (Lesser)obj;
		} else if (obj instanceof Greater) {
			greater = (Greater)obj;
		} else {
			E.error("cant add " + obj);
		}

	}


	public boolean getBValue(EvaluationContext ectxt) {
		double gre = greater.getValue(ectxt);
		double les = lesser.getValue(ectxt);

		boolean ret = false;
		if (les < gre) {
			ret = true;
		}
		return ret;
	}



	public void apply(EvaluationContext ectxt) {
		ectxt.addBoolean(name, getBValue(ectxt));
	}

}
