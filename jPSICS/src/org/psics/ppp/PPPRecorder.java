package org.psics.ppp;

import java.io.BufferedReader;
import java.io.IOException;

import org.psics.be.E;


public class PPPRecorder {

	String id;

	int target;
	int type;

	double[] xyz;

	public void populateFrom(BufferedReader br) throws IOException {
			String sid = br.readLine();
			id = sid.trim();
			String ttnp = br.readLine();
			int[] wk = PPP.readInts(ttnp, 3);
			target = wk[0];
			type = wk[1];

	}


	public String getRecorderTypeString() {
		String ret = null;
		if (type == 1) {
			ret = "V";
			
		} else if (type == 2) {
			ret = "I";
			
			
		} else if (type == 3) {
			ret = "C";
			
		} else if (type == 4) {
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
