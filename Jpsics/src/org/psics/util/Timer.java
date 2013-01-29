package org.psics.util;

import org.psics.be.E;


public class Timer {

	long startTime;

	private static long getTime() {
		return System.currentTimeMillis();
	}


	public Timer() {
		startTime = getTime();
	}



	public void show(String msg) {
		long tnow = getTime();
		int dt = (int)(tnow - startTime);

		E.info("Timing: " + dt + " " + msg);
	}

	public void showSlow(String msg) {
		long tnow = getTime();
		int dt = (int)(tnow - startTime);
		if (dt > 100) {
			E.info("Timing: " + dt + " " + msg);
		}
	}


}
