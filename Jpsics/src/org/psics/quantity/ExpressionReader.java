package org.psics.quantity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.psics.be.E;
import org.psics.quantity.units.Units;

public class ExpressionReader {


	static Pattern pat = Pattern.compile("(^[\\d\\.\\+-]*(?:[\\+-]?[Ee][\\d]+)?)(.*?)$");
	public static void populate(DimensionalExpression dq, String argin, Units dfltUnits) {

		 String[] sa = getSplit(argin);
		// E.info("split expression into " + sa[0] + " and " + sa[1]);

		 if (sa[1] != null) {
			 Units u = findUnits(sa[1]);
			 if (u == null) {
				 // already reported;
				 dq.setValue(sa[0], dfltUnits);
			 } else {
				 dq.setValue(sa[0], u);
			 }
		 } else {
			 dq.setValue(sa[0], dfltUnits);
		 }
	}


	public static String[] getSplit(String argin) {
		String se = null;
		String su = null;

		 String arg = argin;
		 if (arg == null) {
			 arg = "";
		 }
		 arg = arg.trim();
		 int ils = arg.lastIndexOf(" ") + 1;
		 int icb = arg.lastIndexOf(")") + 1;
		 if (icb > ils) {
			 ils = icb;
		 }

		 Matcher matcher = pat.matcher(arg);
		 if (matcher.find()) {
			String snum = matcher.group(1);
		     if (snum.length() > ils) {
		    	 ils = snum.length();
		     }
		 }



		 if (ils > 0 && ils <= arg.length()) {
			 su = arg.substring(ils, arg.length());
			 se = arg.substring(0, ils);
		 }
		 if (su.length() == 0) {
			 su = null;
		 }
		 String[] ret = {se, su};
		 //  E.info("expression is===" + se + "==== units are ===" + su);
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


	public static void main(String[] argv) {
		String[] tests = {"0.1 per_um2", "1.3 (p + 4) m", "222um"};
		for (String s : tests) {
			String[] sa = getSplit(s);
			E.info("split " + s + " into " + sa[0] + "    and    " + sa[1]);
		}
	}



}
