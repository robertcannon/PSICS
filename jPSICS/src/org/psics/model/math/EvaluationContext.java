package org.psics.model.math;

import java.util.HashMap;

import org.psics.be.E;

public class EvaluationContext {

	HashMap<String, RVal> values = new HashMap<String, RVal>();

	HashMap<String, BVal> bvalues = new HashMap<String, BVal>();

	HashMap<String, Function> functions = new HashMap<String, Function>();


	public EvaluationContext() {

	}


	public double getDouble(String s) {
		 double ret = 0.;
		 if (values.containsKey(s)) {
			 ret = values.get(s).getValue();
		 } else {
			 	E.info("require " + s + " but it is not known ");
				StringBuffer sb = new StringBuffer();
			 	for (String sk : values.keySet()) {
					 sb.append(sk);
					 sb.append(", ");
			 	}

				 E.fatalError("know of: " + sb.toString() + "   exiting");

		 }
		 return ret;
	}


	public void addDouble(String name, double val) {
		 values.put(name, new RVal(name, val));
	}


	public boolean getBoolean(String test) {
		boolean ret = false;
		if (bvalues.containsKey(test)) {
			ret = bvalues.get(test).getValue();
		} else {
			E.error("no such boolean " + test);
		}
		return ret;
	}


	public void addBoolean(String name, boolean val) {
		 bvalues.put(name, new BVal(name, val));
	}


	public void addFunction(Function f) {
		 functions.put(f.getID(), f);
	}


	public boolean hasFunction(String sf) {
		return (functions.containsKey(sf));
	}

	public Function getFunction(String sf) {
		return functions.get(sf);
	}

}
