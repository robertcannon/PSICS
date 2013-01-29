package org.psics.fort;

public class FUtil {

	
	public static String writeLineArray(String[] sa) {
		StringBuffer sb = new StringBuffer();
		int n = sa.length;
		for (int i = 0; i < n; i++) {
			sb.append(sa[i]);
			if (i < n-1) {
				sb.append(", ");
			}
		}
		sb.append("\n");
		return sb.toString();
	}
	
	public static String writeLineArray(double[] da, String fstring) {
		StringBuffer sb = new StringBuffer();
		int n = da.length;
		for (int i = 0; i < n; i++) {
			sb.append(String.format(fstring, da[i]));
			if (i < n-1) {
				sb.append(", ");
			}
		}
		sb.append("\n");
		return sb.toString();
	}
}
