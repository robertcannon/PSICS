package org.psics.codgen.channel;

import org.psics.be.TransitionEvaluator;

public class TauInfCodedTransitionEvaluator extends CodedTransitionEvaluator implements TransitionEvaluator {


	String tiString;



	public TauInfCodedTransitionEvaluator(TauInfCodedTransitionEvaluator tice) {
		super(tice);
	}


	public TauInfCodedTransitionEvaluator(String cnm, String tauvar, String infvar, String[][] constants, String src) {
		className = cnm;
		StringBuffer srcSB = new StringBuffer();


		srcSB.append("public double[] getTauInf(double v, double temperature) {\n");

		if (constants != null) {
			for (String[] sa : constants) {
				srcSB.append(" double " + sa[0] + " = " + sa[1] + ";\n");
			}
		}

		srcSB.append("  double " + tauvar + " = 0.;\n");
		srcSB.append("  double " + infvar + " = 0.;\n");

		srcSB.append(src);
		srcSB.append("\n");

		srcSB.append("   double[] ret = {" + tauvar + ", " + infvar + "};\n");
		srcSB.append("   return ret;\n");
		srcSB.append("}\n");
		tiString = srcSB.toString();
	}



	public double[] getBaseAlphaBeta(double v, double temperature, Object eval) {
		TauInfEvaluator tie = (TauInfEvaluator)eval;
		double[] xti = tie.getTauInf(v, temperature);
		double xtau = xti[0];
		double xinf = xti[1];


		double alpha = xinf / xtau;
		double beta = (1. - xinf) / xtau;

		double[] ret = {alpha, beta};
		return ret;
	}



	public CodedTransitionEvaluator makeCopy() {
		return new TauInfCodedTransitionEvaluator(this);
	}


	public String getMethodString() {
		 return tiString;
	}


	public String getInterfaceName() {
		return "TauInfEvaluator";
	}





}
