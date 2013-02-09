package org.psics.util;


public class DblIdx implements Comparable<Object> {
		   double dbl;
		   int idx;

		   public DblIdx(double d, int i) {
			   dbl = d;
			   idx = i;
		   }

		public int compareTo(Object o) {
			DblIdx dc = (DblIdx)o;
			int ret = 0;
			if (dbl < dc.dbl) {
				ret = -1;
			} else if (dbl > dc.dbl) {
				ret = 1;
			}
			return ret;
		}


}
