package org.psics.model.channel;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.codgen.channel.CodedTransitionEvaluator;
import org.psics.model.Constant;
import org.psics.num.model.channel.TransitionType;
import org.psics.quantity.annotation.Container;


public abstract class CodedTransition extends KSTransition {

	@Container(contentTypes = {Constant.class}, tag = "")
	public ArrayList<Constant> constants = new ArrayList<Constant>();


	ArrayList<CodedTransitionFunction> functions;


	protected double fwdFactor = 1.;
	protected double revFactor = 1.;

	private CodedTransition refTransition = null;

	private CodedTransitionEvaluator p_evaluator;

	private String codeFragment;




	public void setCodeFragment(String s) {
		codeFragment = s;
	}


	public void setFunctions(ArrayList<CodedTransitionFunction> ctfa) {
		functions = ctfa;
	}

	public String getCodeFragment() {
		return codeFragment;
	}

	public void add(Object obj) {
		if (obj instanceof Constant) {
			constants.add((Constant)obj);

		}  else {
			E.error("cant add " + obj);
		}
	}



	public TransitionType getTransitionType() {
		return TransitionType.CODED;
	}



	public double[] getTransitionData() {
	    double[] ret = new double[4];
	    writeTempDependence(ret);
	    ret[2] = fwdFactor;
	    ret[3] = revFactor;
		return ret;
	}



	protected String[][] getConstantsArray() {
		String[][] ret = new String[constants.size()][2];
		for (int i = 0; i < constants.size(); i++) {
			Constant c = constants.get(i);
			ret[i][0] = c.getName();
			ret[i][1] = "" + c.getValue();
		}
		return ret;
	}

	public void setBodyValue(String s) {
		codeFragment = s;
	}

	public void applyMultipliers(double fm, double rm) {
		fwdFactor *= fm;
		revFactor *= rm;
	}


	public abstract CodedTransition makeLocalCopy();



	public CodedTransition makeCopy(KSState sa, KSState sb) {
		CodedTransition ret = makeLocalCopy();
		ret.setEnds(sa, sb);

		CodedTransition rtr = this;
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


	// N.B. twh copies here: shallow used for expanding out multi-complex chanels, so that we
	// end up using the same evaluator object.
	// deep is used for derived channels where one is like another but with some parameter changes
	// in coded transitions. All copies for non coded transitions are deep
	public CodedTransition makeMultiCopy(KSState sa, KSState sb, double ff, double fr) {
		 CodedTransition ret = makeCopy(sa, sb);
		 ret.applyMultipliers(ff, fr);
		 return ret;
	}



	public KSTransition deepCopy(KSState sa, KSState sb) {
		 CodedTransition ret = makeCopy(sa, sb);

		 for (Constant c : constants) {
			 ret.add(c.makeCopy());
		 }

		 ArrayList<CodedTransitionFunction> ctfa = new ArrayList<CodedTransitionFunction>();
		 for (CodedTransitionFunction f : functions) {
			 ctfa.add(f.deepCopy());
		 }
		 ret.setFunctions(ctfa);

		 ret.codeFragment = codeFragment;
		 ret.refTransition = null;
		 return ret;
	}


	public abstract CodedTransitionEvaluator makeEvaluator();



	public CodedTransitionEvaluator getEvaluator() {
		if (p_evaluator == null) {
			if (refTransition == null) {
				p_evaluator = makeEvaluator();
				p_evaluator.compileAndLoad();

				if (p_evaluator.ready()) {
				// OK
				} else {
					E.fatalError("compilation problems for transition " + getSysID() + " " + codeFragment);
				}
			} else {
				p_evaluator = refTransition.getEvaluator().makeCopy();
			}
			p_evaluator.setMultipliers(fwdFactor, revFactor);
		}


		return p_evaluator;
	}




}
