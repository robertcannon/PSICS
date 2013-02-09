package org.psics.model.math;

import org.psics.be.AddableTo;
import org.psics.be.E;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;


public class Function implements AddableTo {

	public String name;

	public String args;


	String[] parsedArgs;


	public ArrayList<Assignment> assignments = new ArrayList<Assignment>();

	public Return ret;


	public void add(Object obj) {
		if (obj instanceof Return) {
			ret = (Return)obj;
		} else if (obj instanceof Assignment) {
			assignments.add((Assignment)obj);
		} else {
			E.error("cant add " + obj);
		}
	}


	public double evaluate(double[] da) {
		if (parsedArgs == null) {
			parsedArgs = parseArgs(args);
		}

		if (da.length != parsedArgs.length) {
			E.error("cant evaluate - arg lengt mismatch " + da.length + " " +
					parsedArgs.length);
			return 0.;

		}

		EvaluationContext ec = new EvaluationContext();
		for (int i = 0; i < da.length; i++) {
			ec.addDouble(parsedArgs[i], da[i]);
		}
		for (Assignment ass : assignments) {
			ass.apply(ec);
		}

		double r = 0.;
		if (ret == null) {
			E.error("no return");
		} else {
			r = ret.getValue(ec);
		}
		return r;
	}



	static String[] parseArgs(String s) {
		StringTokenizer st = new StringTokenizer(s, ", ");
		int nt = st.countTokens();
		String[] ret = new String[nt];
		for (int i = 0; i < nt; i++) {
			ret[i] = st.nextToken();
		}
		return ret;
	}


	public String getID() {
		 return name;
	}


	public double evaluate(ArrayList<Arg> aa, EvaluationContext pctxt) {
		if (parsedArgs == null) {
			parsedArgs = parseArgs(args);
		}

		HashSet<String> hs = new HashSet<String>();
		for (String s : parsedArgs) {
			hs.add(s);
		}

		EvaluationContext ec = new EvaluationContext();
		for (Arg a : aa) {
			String snm = a.getName();
			double d = a.getValue(pctxt);
			ec.addDouble(snm, d);


			if (hs.contains(snm)) {
				hs.remove(snm);
			} else {
				E.fatalError("the supplied argument, " + snm + ", does not match any actual argument");
			}
		}
		if (hs.size() == 0) {
			// OK;
		} else {
			E.fatalError("missing argument calling function " + name + ": requires " + hs);
		}


		for (Assignment ass : assignments) {
			ass.apply(ec);
		}

		double r = 0.;
		if (ret == null) {
			E.error("no return");
		} else {
			r = ret.getValue(ec);
		}
		return r;
	}

}
