package org.psics.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.catacomb.numeric.data.DataTable;
import org.psics.be.E;


public class PSICSDataReader {

	public static DataTable readTable(File f) {
		DataTable ret = new DataTable();

		try {
		   BufferedReader br = new BufferedReader(new FileReader(f));
		   String line = "";
		   while (br.ready()) {
			  line = br.readLine();
			  String lt = line.trim();
              if (lt.startsWith("#") || lt.length() == 0) {
            	  // skip it;
              } else {
            	  break;
              }
		   }
//		   E.info("read a proper line " + line);
           StringTokenizer st = new StringTokenizer(line, " ,;\t");
           int nrec = Integer.parseInt(st.nextToken());
           int npts = Integer.parseInt(st.nextToken());
           int nrun = Integer.parseInt(st.nextToken());

           int nrr = 20;
           E.info("file contains " + nrun + " sequential runs - only reading first " + nrr);
           ret.setNColumn(nrec * nrr + 1);
           // TODO: in fact, we'll only read the first 20 runs for now
           double[] tpts = readArray(br, npts);

           double[][] dat = new double[nrr][];
           for (int i = 0; i < nrr; i++) {
        	   dat[i] = readArray(br, nrec * npts);
           }

           for (int i = 0; i < npts; i++) {
        	   double[] row = new double[nrec * nrr + 1];
        	   row[0] = tpts[i];
        	   for (int j = 0; j < nrr; j++) {
        		   for (int k = 0; k < nrec; k++) {
        			   row[1 + nrec * j + k] = dat[j][i * nrec + k];
        		   }
        	   }

        	   ret.addRow(row);
           }

		} catch (IOException ex) {
			E.error("read error: " + ex);
		}
		ret.close();
		return ret;
	}


	private static double[] readArray(BufferedReader br, int n) throws IOException {
		double[] ret = new double[n];
		int nread = 0;
		while (nread < n) {
			String line = br.readLine();
			StringTokenizer st = new StringTokenizer(line, " ,;\t[]");
			int ntok = st.countTokens();
			for (int i = 0; i < ntok && nread < n; i++) {
				ret[nread] = Double.parseDouble(st.nextToken());
				nread += 1;
			}
		}
		return ret;
	}


}
