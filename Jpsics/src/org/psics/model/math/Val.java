package org.psics.model.math;

import org.psics.be.BodyValued;

public class Val implements RealValued, BodyValued {

	String p_vname;


	public double getValue(EvaluationContext ectxt) {
		 return ectxt.getDouble(p_vname);
	}


	public void setBodyValue(String s) {
		p_vname = s.trim();
	}



}
