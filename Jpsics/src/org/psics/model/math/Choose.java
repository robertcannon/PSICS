package org.psics.model.math;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class Choose extends UnaryExpression implements AddableTo {

	ArrayList<When> whens = new ArrayList<When>();

	Otherwise otherwise = null;

	public void add(Object obj) {
		if (obj instanceof When) {
			whens.add((When)obj);
		} else if (obj instanceof Otherwise) {
			otherwise = (Otherwise)obj;
		} else {
			E.error("cant add " + obj);
		}
	}


	public double getValue(EvaluationContext ectxt) {
		double ret = 0.;
		boolean got = false;
		for (When w : whens) {
			if (w.applies(ectxt)) {
				ret = w.getValue(ectxt);
				got = true;
				break;
			}
		}
		if (!got && otherwise != null) {
			ret = otherwise.getValue(ectxt);
		}
		return ret;
	}


}
