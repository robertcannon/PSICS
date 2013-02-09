package org.psics.model.math;

import org.psics.be.BodyValued;

public class Con implements RealValued, BodyValued {

	double p_value;

	public double getValue(EvaluationContext ectxt) {
		 return p_value;
	}


	public void setBodyValue(String s) {
		p_value = Double.parseDouble(s);
	}



}
