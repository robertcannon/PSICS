package org.psics.codgen.channel;

import org.psics.be.TransitionEvaluator;
import org.psics.codgen.EvaluatorFactory;


public abstract class CodedTransitionEvaluator implements TransitionEvaluator {

	String className;

	String funcString;
	double fwdFactor = 1;
	double revFactor = 1;

	boolean compiledOK = false;


	CodedTransitionEvaluator refEval;

	Object evaluatorObject;


	public CodedTransitionEvaluator() {

	}


	public CodedTransitionEvaluator(CodedTransitionEvaluator ref) {
		refEval = ref;
	}

	public void addFunction(String type, String name, String retvar, String[][] args, String src) {

		StringBuffer sb = new StringBuffer();
		sb.append(" public " + type + " " + name + "(");
		if (args != null) {
			boolean first = true;
			for (String[] sa : args) {
				if (!first) {
					sb.append(", ");
				}
				sb.append("" + sa[0] + " " + sa[1]);
				first = false;
			}
		}
		sb.append(") { \n");
		sb.append("  " + type + " " + retvar + " = ");
		if (type.equals("double") || type.equals("int")) {
			sb.append("0;\n");
		} else {
			sb.append("null;\n");
		}


		sb.append(src);
		sb.append("\n");
		sb.append("   return " + retvar + ";\n");
		sb.append("}\n");

		if (funcString == null) {
			funcString = "";
		}
		funcString = funcString + sb.toString();
	}






	public void setMultipliers(double f, double r) {
		fwdFactor = f;
		revFactor = r;
	}


	public abstract String getMethodString();

	public abstract String getInterfaceName();


	public void compileAndLoad() {
		StringBuffer srcSB = new StringBuffer();
		srcSB.append("import org.psics.codgen.channel.TauInfEvaluator;\n");
		srcSB.append("public class " + className + " implements " + getInterfaceName() + " {\n");

		if (funcString != null) {
			srcSB.append(funcString);
		}

		srcSB.append(getMethodString());
		srcSB.append("}\n");

		String src = srcSB.toString();

		EvaluatorFactory evfac = EvaluatorFactory.get();

		evaluatorObject = evfac.instantiateFromString(className, src);
		if (evaluatorObject != null) {
			compiledOK = true;
		}
	}



	public boolean ready() {
		return compiledOK;
	}



	public Object getEvaluatorObject() {
		Object ret = evaluatorObject;
		if (ret == null) {
			if (refEval != null) {
				ret = refEval.getEvaluatorObject();
			} else {
				compileAndLoad();
				ret = evaluatorObject;
			}
		}
		return ret;
	}


	public abstract double[] getBaseAlphaBeta(double v, double temperature, Object obj);


	public double[] alphaBeta(double v, double temperature) {
		double[] ret = getBaseAlphaBeta(v, temperature, getEvaluatorObject());
		ret[0] *= fwdFactor;
		ret[1] *= revFactor;
		return ret;
	}


	public abstract CodedTransitionEvaluator makeCopy();




}
