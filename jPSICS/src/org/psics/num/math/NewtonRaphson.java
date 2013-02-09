package org.psics.num.math;

import org.psics.be.DifferentiableFunction;
import org.psics.be.E;


public class NewtonRaphson {

	DifferentiableFunction func;
	
	
	public NewtonRaphson(DifferentiableFunction f) {
		func = f;
	}
	
	
	public double getRoot(double x0, double tol) {
		double x = x0;
		double xpr = x;
		int ntry = 0;

		while (true) {
			xpr = x;
			double val = func.getValue(x);
			double grad = func.getGradient(x);
			x = x - val / grad;
			ntry += 1;
			
			if (ntry > 40) {
				E.error("cant find root  " + func);
			} else if (x - xpr < tol) {
				break;
			}
		}
		return x;
	}
	
	
	
}
