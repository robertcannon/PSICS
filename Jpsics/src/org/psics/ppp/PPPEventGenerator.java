package org.psics.ppp;

import java.io.BufferedReader;
import java.io.IOException;

import org.psics.be.E;


public class PPPEventGenerator {

	 
	int type;
 
	public void populateFrom(BufferedReader br) throws IOException {
			String ttnp = br.readLine();
			int[] wk = PPP.readInts(ttnp, 3);
			type = wk[0];
			String l2 = br.readLine();
			if (type == 10) {
				wk = PPP.readInts(l2, 1);
				int nel = wk[0];
				for (int i = 0; i < nel; i++) {
					br.readLine();
				}
			}
	}

 


}
