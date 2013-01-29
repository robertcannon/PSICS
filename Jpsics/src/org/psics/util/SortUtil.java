package org.psics.util;

public class SortUtil {

	
	// not for use on big arrays
	public static int[] ascendingIndexes(double[] d) {
		int n = d.length;
		int[] isi = new int[n];

		for (int i = 0; i < n; i++) {
			double din = d[i];
			int iin = 0;
			while (iin < i && din > d[isi[iin]]) {
				iin++;
			}
			for (int k = i - 1; k >= iin; k--) {
				isi[k + 1] = isi[k];
			}
			isi[iin] = i;
		}
		return isi;
	}

}
