package org.psics.model.math;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;

public class FunctionValue implements RealValued, AddableTo, Assignment {

	public String name;
	public String function;

	ArrayList<Arg> args = new ArrayList<Arg>();

	public void add(Object obj) {
		if (obj instanceof Arg) {
			args.add((Arg)obj);
		} else {
			E.error("cant add " + obj);
		}
	}

	public double getValue(EvaluationContext ectxt) {
		if (!ectxt.hasFunction(function)) {
			E.fatalError("no such function " + function);
		}
		Function f = ectxt.getFunction(function);

		double ret = f.evaluate(args, ectxt);

	//	double v = ectxt.getDouble("v");
	//	if (v < -79.9) {
	//		E.info("evald funct to " + ret);
	//	}

		return ret;
	}



	public void apply(EvaluationContext ectxt) {
		double d = getValue(ectxt);
		ectxt.addDouble(name, d);

	}

}
