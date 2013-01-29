package org.psics.util;


public class StringUtil {

	// EFF - not for long padding!
	public static String blankPad(String sp, int totl) {
		StringBuffer sb = new StringBuffer();
		int nta = totl - sp.length();
		sb.append(sp);
		for (int i = 0; i < nta; i++) {
			sb.append(" ");
		}
		return sb.toString();
	}

}
