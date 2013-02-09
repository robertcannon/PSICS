package org.psics.quantity;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.psics.be.E;
import org.psics.be.Parameterized;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.phys.PhysicalCoordinate;
import org.psics.quantity.phys.Length;

import org.psics.quantity.phys.QuantityArray;
import org.psics.quantity.units.Units;

public class QuantityReader {


	static String unitsrex = "([:alpha: _]*)$";
	// static Pattern pat = Pattern.compile("(^[\\d\\.\\+-]*)({[Ee]?[\\+-]?[\\d]+}?)(.*?)$");
	static Pattern pat = Pattern.compile("(^[\\d\\.\\+-]*(?:[Ee][\\+-]?[\\d]+)?)(.*?)$");
	
	static Pattern exppat = Pattern.compile("\\((.*)\\)(.*?)$");


	public static void populate(DimensionalQuantity dq, String arg, Units dfltUnits) {
		
		if (arg.trim().length() == 0) {
			dq.setNoValue();
			
		} else {
		
		Matcher matcher = pat.matcher(arg);
		
	     if (matcher.find()) {
	    	//  int start = matcher.start();

	    	 String snum = matcher.group(1); //  + matcher.group(2);
	    	 String su = matcher.group(2);
     	 
	 
	    	 double d = Double.parseDouble(snum);

	    	 if (su == null || su.length() <= 0) {
	    		 if (dq instanceof PhysicalCoordinate || dq instanceof Length ||
	    				 dq instanceof NDValue || dq instanceof NDNumber) {
	    			 // dont warn - POSERR
	    		 } else {
	    			 E.oneLineWarning("no units for " + dq.getName() + " being set from " + arg + " defaulting to " + dfltUnits);
	    		 }
	    		 dq.setValue(d, dfltUnits);
	    	 } else {
	    		 Units u = findUnits(su);
	    		 if (u == null) {
	    		 // already reported;
	    			 dq.setValue(d, dfltUnits);
	    		 } else {
	    			 dq.setValue(d, u);
	    		 }
	    	 }

	    	 dq.setOriginalText(arg);
	    	 
	     } else {
	    	 E.warning("cant parse " + arg + " for quantity " + dq + " (" + dfltUnits + ")");

	     }
		}
	}

 
		public static void paramPopulate(DimensionalQuantity dq, String arg, 
				Units dfltUnits, Parameterized ptzd) {
			
			if (arg.trim().length() == 0) {
				dq.setNoValue();
				
			} else {
			
			Matcher matcher = exppat.matcher(arg);
			
		     if (matcher.find()) {
		    	//  int start = matcher.start();

		    	 String sexp = matcher.group(1); //  + matcher.group(2);
		    	 String su = matcher.group(2);
	     	  
		       	 E.info("exppat split " + arg + " into " + sexp + " and " + su);
		    	 
		    	 Units u = dfltUnits;
		    	 
		    	 if (su == null || su.length() <= 0) {
		    		 if (dq instanceof PhysicalCoordinate || dq instanceof Length ||
		    				 dq instanceof NDValue || dq instanceof NDNumber) {
		    			 // dont warn - POSERR
		    		 } else {
		    			 E.oneLineWarning("no units for " + dq.getName() + " being set from " + arg + " defaulting to " + dfltUnits);
		    		 }
		    		 
		    	 } else {
		    		 u = findUnits(su);
		    		 if (u == null) {
		    			 u = dfltUnits;
		    			 // already reported;
		    			 
		    		 } else {
		    			 // OK as is
		    		 }
		    	 }

		    	 double d = expEval(sexp, ptzd);
		    	 dq.setValue(d, u);
		    	 
		    	 dq.setOriginalText(arg);
		    	 
		     } else {
		    	 E.warning("cant parse " + arg + " for quantity " + dq + " (" + dfltUnits + ")");

		     }
			}
		}


		
	
		public static double expEval(String sexp, Parameterized ptzd) {
			// get the value of expression, sexp, in units u, using quantities
		    // defined in the parameterized obj ptzd
			
			double ret = 0.;
			HashMap<String, Double> vars = ptzd.getVariables();
			
			if (sexp.indexOf("(") >= 0. || sexp.indexOf("+") >= 0 || 
					sexp.indexOf("-") >= 0 || sexp.indexOf("*") >= 0 || sexp.indexOf("/") >= 0) {
				Evaluator eve = new Evaluator(sexp, ptzd.getVariables());
				ret = eve.getValue();
				
			} else {
				if (vars.containsKey(sexp)) {
					ret = vars.get(sexp).doubleValue();
				} else {
					E.error("unrecognized parameter: " + sexp);
				}
			}
		 
			return ret;
		}
	
	
	

	public static Units findUnits(String su) {
		Units ret = null;

		ret = Units.getByLabel(su);
		if (ret == null) {
			E.error("cant parse units " + su);
			// TODO could be a compound - need to parse it...
		}

		return ret;
	}



	public static void populateArray(QuantityArray qa, String s) {
		int isl = s.lastIndexOf("]");
		String su = s.substring(isl+1, s.length());

		if (su != null && su.length() > 0) {
			Units u = findUnits(su);
			qa.setUnits(u);
		} else {
			qa.setUnits(Units.none);
		}

		// Specific to 1d araysl...
		StringTokenizer st = new StringTokenizer(s.substring(0, isl), " ,][");
		int nt = st.countTokens();
		double[] v = new double[nt];
		String[] sv = new String[nt];
		for (int i = 0; i < nt; i++) {
			String tok = st.nextToken();
			sv[i] = tok;
			v[i] = Double.parseDouble(tok);
		}
		qa.setValues(v);
		qa.setStringValues(sv);

	}


}
