package org.psics.codgen.channel;

import org.psics.be.TransitionEvaluator;

public class AlphaBetaCodedTransitionEvaluator extends CodedTransitionEvaluator implements TransitionEvaluator {


	String tiString;



	public AlphaBetaCodedTransitionEvaluator(AlphaBetaCodedTransitionEvaluator tice) {
		super(tice);
	}


	public AlphaBetaCodedTransitionEvaluator(String cnm, String avar, String bvar, String[][] constants, String src) {
		className = cnm;
		StringBuffer srcSB = new StringBuffer();


		srcSB.append("public double[] getTauInf(double v, double temperature) {\n");

		if (constants != null) {
			for (String[] sa : constants) {
				srcSB.append(" double " + sa[0] + " = " + sa[1] + ";\n");
			}
		}

		srcSB.append("  double " + avar + " = 0.;\n");
		srcSB.append("  double " + bvar + " = 0.;\n");

		srcSB.append(src);
		srcSB.append("\n");

		srcSB.append("   double[] ret = {" + avar + ", " + bvar + "};\n");
		srcSB.append("   return ret;\n");
		srcSB.append("}\n");
		tiString = srcSB.toString();
	}



	public double[] getBaseAlphaBeta(double v, double temperature, Object eval) {
		AlphaBetaEvaluator abe = (AlphaBetaEvaluator)eval;
		double[] xti = abe.getAlphaBeta(v, temperature);
		double xa = xti[0];
		double xb = xti[1];
		double[] ret = {xa, xb};
		return ret;
	}



	public CodedTransitionEvaluator makeCopy() {
		return new AlphaBetaCodedTransitionEvaluator(this);
	}


	public String getMethodString() {
		 return tiString;
	}


	public String getInterfaceName() {
		return "TauInfEvaluator";
	}





}
