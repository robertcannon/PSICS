package org.psics.quantity;

import java.util.HashMap;
import java.util.Map;

import org.nfunk.jep.JEP;
import org.psics.be.E;


// wrap up jep in case we want to use something else

public class Evaluator {

	JEP jep;
	boolean expOK;
	String errorMessage = "";

	
	public Evaluator(String expression, HashMap<String, Double> variables) {
			jep = new JEP();

			jep.addStandardFunctions();
			jep.addStandardConstants();   // pi etc

			for (Map.Entry<String, Double> me : variables.entrySet()) {
				String sk = me.getKey();
				double val = me.getValue().doubleValue();
				jep.addVariable(sk, val);
				// E.info("Added variable " + sk + " " + val);
			}
			try {
				expOK = true;
				jep.parseExpression(expression);

			//	E.info("jep parsed " + expression  + " " + jep.getValue());

				if(jep.hasError()) {
					expOK = false;
					errorMessage = jep.getErrorInfo();
					E.info("jep error " + errorMessage + " in " + expression);
				}


			} catch (Exception ex) {
				expOK = false;
				errorMessage = ex.getMessage();
			}
	}


	public boolean valid() {
		return expOK;
	}

	
	public double getValue() {
		return jep.getValue();
	}
	
	public double getValue(HashMap<String, Double> variables) {
		for (Map.Entry<String, Double> me : variables.entrySet()) {
			jep.setVarValue(me.getKey(), me.getValue().doubleValue());
		}
		double ret = jep.getValue();
		return ret;
	}


	public String getErrorMessage() {
		return errorMessage;
	}


	public boolean getBoolean(HashMap<String, Double> variables) {
		for (Map.Entry<String, Double> me : variables.entrySet()) {
			jep.setVarValue(me.getKey(), me.getValue().doubleValue());
		}
		double ret = jep.getValue();
		// E.info("seeking boolean got val " + ret);
		return (ret > 0.5);
	}


}
