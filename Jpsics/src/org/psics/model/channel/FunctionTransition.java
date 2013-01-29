package org.psics.model.channel;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.be.TransitionEvaluator;
import org.psics.model.Constant;
import org.psics.model.math.Assignment;
import org.psics.model.math.EvaluationContext;
import org.psics.model.math.Function;
import org.psics.num.model.channel.TransitionType;
import org.psics.quantity.annotation.Container;


public abstract class FunctionTransition extends KSTransition implements TransitionEvaluator {

	@Container(contentTypes = {Constant.class}, tag = "")
	public ArrayList<Constant> constants = new ArrayList<Constant>();

	@Container(contentTypes = {Function.class}, tag = "")
	ArrayList<Function> funcs;

	@Container(contentTypes = {Assignment.class}, tag="")
	ArrayList<Assignment> assignments = new ArrayList<Assignment>();


	protected double fwdFactor = 1.;
	protected double revFactor = 1.;

	private FunctionTransition refTransition = null;




	public void setFunctions(ArrayList<Function> ctfa) {
		if (funcs != null) {
			E.error("overwriting functions?");
		}
		funcs= ctfa;
	}


	public void addConstant(Constant c) {
		constants.add(c);
	}

	public void addAssignment(Assignment a) {
		assignments.add(a);
	}




	public TransitionType getTransitionType() {
		return TransitionType.FUNCTION;
	}



	public double[] getTransitionData() {
	    double[] ret = new double[4];
	    writeTempDependence(ret);
	    ret[2] = fwdFactor;
	    ret[3] = revFactor;
		return ret;
	}



	public void applyMultipliers(double fm, double rm) {
		fwdFactor *= fm;
		revFactor *= rm;
	}


	public abstract FunctionTransition makeLocalCopy();



	public FunctionTransition makeCopy(KSState sa, KSState sb) {
		FunctionTransition ret = makeLocalCopy();
		ret.setEnds(sa, sb);

		ret.constants = constants;
		ret.funcs = funcs;
		ret.assignments = assignments;

		FunctionTransition rtr = this;
		while (rtr.refTransition != null) {
			rtr = rtr.refTransition;
		}
		ret.refTransition = rtr;
	//	E.info("copied coded transition from " + hashCode() + " to " + ret.hashCode() + " " + rtr.getSysID());


		ret.fwdFactor = fwdFactor;
		ret.revFactor = revFactor;
		copyTemperatureTo(ret);
		return ret;
	}


	// N.B. two copies here: shallow used for expanding out multi-complex chanels, so that we
	// end up using the same evaluator object.
	// deep is used for derived channels where one is like another but with some parameter changes
	// in coded transitions. All copies for non coded transitions are deep
	public FunctionTransition makeMultiCopy(KSState sa, KSState sb, double ff, double fr) {
		 FunctionTransition ret = makeCopy(sa, sb);
		 ret.applyMultipliers(ff, fr);
		 return ret;
	}



	public KSTransition deepCopy(KSState sa, KSState sb) {
		 FunctionTransition ret = makeCopy(sa, sb);

		 for (Constant c : constants) {
			 ret.addConstant(c.makeCopy());
		 }

		 // TODO shallow copy OK??
 		 ret.setFunctions(funcs);

		 ret.refTransition = null;
		 return ret;
	}


	public EvaluationContext buildContext(double v, double temperature) {
		EvaluationContext ectxt = new EvaluationContext();
		ectxt.addDouble("v", v);
		ectxt.addDouble("temperature", temperature);
		for (Constant c : constants) {
			c.set(ectxt);
		}
		if (funcs != null) {
			for (Function f : funcs) {
				// E.info("adding function " + f.getID());
				ectxt.addFunction(f);
			}
		}
		for (Assignment a : assignments) {
			a.apply(ectxt);
		}
		return ectxt;
	}


	public void addFunctions(ArrayList<Function> c_funcs) {
		if (c_funcs != null) {
			if (funcs == null) {
				funcs = new ArrayList<Function>();
			}
			funcs.addAll(c_funcs);
		}
	}




}
