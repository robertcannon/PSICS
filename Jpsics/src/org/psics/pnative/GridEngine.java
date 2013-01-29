package org.psics.pnative;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import org.psics.be.E;
import org.psics.util.JUtil;


public class GridEngine {

	static boolean doneCheck = false;
	static boolean hasGE;



	public static void checkAvailable() {
		if (doneCheck) {
			return;
		} else {
			String osarch = JUtil.getOSArchitecture();

			if (osarch.startsWith("linux") || osarch.startsWith("mac")) {
				String cmd = "which qsub";
				String sout = "";

				try {

					Process p = Runtime.getRuntime().exec(cmd);
					BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
					String lin;
					int iwf = p.waitFor();
					while ((lin = br.readLine()) != null) {
						sout += lin;
					}
					if (iwf != 0) {
						// no such command as qsub
						sout = "";
					}

				} catch (Exception ex) {
					E.error("" + ex);
				}

				// E.info("qsub check got " + sout);
				if (sout.endsWith("/qsub")) {
					hasGE = true;
					E.info("GridEngine detected: will use " + sout);
				} else {
					hasGE = false;
				}

			}
		}

	}



	public static boolean isAvailable() {
		checkAvailable();
		return hasGE;
	}




}
