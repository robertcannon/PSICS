package org.psics.ppp;

import java.io.BufferedReader;
import java.io.IOException;

import org.psics.be.E;


public class PPPClamp {

	String id;

	int target;
	int type;
	int nprof;
	boolean record;

	double[] xyz;

	
	public void populateFrom(BufferedReader br) throws IOException {
		String sid = br.readLine();
		id = sid.trim();
		// E.info("read clamp " + id);
		String ttnp = br.readLine();
		int[] wk = PPP.readInts(ttnp, 4);
		target = wk[0];
		type = wk[1];
		if (wk[2] == 1) {
			record = true;
		} else {
			record = false;
		}
		nprof = wk[3];
		if (type == 2) {
			String spot = br.readLine();  // the potential on its own line for a current clamp
			//double[] pota = PPP.readDoubles(pdat, 1);
		}
		for (int i = 0; i < nprof; i++) {
			String pdat = br.readLine();
			double[] pwk = PPP.readDoubles(pdat, 6);
			int ntvt = (int)(Math.round(pwk[4] + 0.1));
			int npl = (int)(Math.round(pwk[5] + 0.1));
			if (ntvt > 0) {
				int nlin = (ntvt-1) / npl + 1;

				for (int idl = 0; idl < nlin; idl++) {
					br.readLine();
				}
			}
		}
	}


	public String getClampTypeString() {
		String ret = null;
		if (type == 0) {
			ret = "I";
		} else if (type == 1) {
			ret = "V";

		} else if (type == 2) {
			ret = "G";

		} else {
			E.error("unrecognized type " + type);
			ret = "err";
		}
		return ret;

	}


	public int getTarget() {
		return target;
	}


	public void setPosition(double[] pos) {
		 xyz = pos;
	}

	public double[] getPosition() {
		return xyz;
	}


	public String getID() {
		return id;
	}

}
