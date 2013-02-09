package org.psics.fort;

import java.util.StringTokenizer;


public class LineDataReader {

	String[] lines;
	int lineCounter;
	
	public LineDataReader(String s) {
		StringTokenizer st = new StringTokenizer(s, "\n");
		int n = st.countTokens();
		lines = new String[n];
		for (int i = 0; i < n; i++) {
			lines[i] = st.nextToken();
		}
		lineCounter = 0;
	}
	
	
	
	private String nextLine() {
		String ret = null;
		if (lineCounter < lines.length) {
			ret = lines[lineCounter];
			lineCounter += 1;
		}
		return ret;
	}
	
	
	public String[] readStrings(int n) throws FormattedDataException {
		String lin = nextLine();
		String[] ret = new String[n];
		if (lin == null) {
			throw new FormattedDataException("end of line source");
		} else {
			StringTokenizer st = new StringTokenizer(lin, " ");
			if (st.countTokens() >= n) {
				for (int i = 0; i < n; i++) {
					ret[i] = st.nextToken();
				}
				
			} else {
				throw new FormattedDataException("insufficient data: need " + n + " from " + lin);
			}
		}
		return ret;	
	}
	
	public int[] readInts(int n) throws FormattedDataException {
		 String[] sa = readStrings(n);
		 int[] ret = new int[n];
		 for (int i = 0; i < n; i++) {
			 ret[i] = Integer.parseInt(sa[i]);
		 }
		 return ret;
	}
	
	public double[] readDoubles(int n) throws FormattedDataException {
		 String[] sa = readStrings(n);
		 double[] ret = new double[n];
		 for (int i = 0; i < n; i++) {
			 ret[i] = Double.parseDouble(sa[i]);
		 }
		 return ret;
	}



	public int readInt() throws FormattedDataException {
		String s = readStrings(1)[0];
		return Integer.parseInt(s);
	}
	
}
