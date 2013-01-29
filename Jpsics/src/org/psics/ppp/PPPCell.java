package org.psics.ppp;

import java.io.BufferedReader;
import java.io.IOException;

import org.psics.be.E;


public class PPPCell {

	int ncomp;

	double[][] xyz;

	double[][] axayaz;

	public void populateFrom(BufferedReader br) throws IOException {
		String snc = br.readLine();
		ncomp = PPP.readInt(snc);
		
		xyz = new double[ncomp][3];

		for (int i = 0; i < ncomp; i++) {
			String sints = br.readLine();
			int[] wk = PPP.readInts(sints, 4);
			int ncon = wk[1];
			String sdbls = br.readLine();

			int ndbl = 2 * ncon + 2 + 3;  // icon, gcon,   v, cap,    posx, posy, posz
			double[] dbls = PPP.readDoubles(sdbls, ndbl);
			xyz[i][0] = dbls[dbls.length - 3];
			xyz[i][1] = dbls[dbls.length - 2];
			xyz[i][2] = dbls[dbls.length - 1];
		}
	}


	public int getNCompartments() {
		 return ncomp;
	}


	public double[] getPosition(int target) {
		 return xyz[target];
	}


	public double[][] getCompartmentPositionArrays() {
		if (axayaz == null) {
			axayaz = new double[3][ncomp];
			for (int i = 0; i < ncomp; i++) {
				axayaz[0][i] = xyz[i][0];
				axayaz[1][i] = xyz[i][1];
				axayaz[2][i] = xyz[i][2];
			}
		}
		return axayaz;
	}

}
