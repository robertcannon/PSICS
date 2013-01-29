package org.psics.ppp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.psics.be.E;

public class PPP {


	int ncommand;


	PPPCell pppCell;



	ArrayList<PPPClamp> clamps = new ArrayList<PPPClamp>();
	ArrayList<PPPRecorder> recorders = new ArrayList<PPPRecorder>();

	public PPP() {

	}


	public static void main(String[] argv) {
		File f = new File("/home/rcc/PSICS/eclipse/PSICS/psics-out/migliore-1a/psics-out.ppp");
		PPP ppp = new PPP();
		ppp.populateFrom(f);
		E.info("Read ppp " +  ppp.getNCompartments());
	}



	public int getNCompartments() {
		return pppCell.getNCompartments();
	}


	public void populateFrom(File f) {
		E.info("reading " + f);
		boolean OK = true ;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));

			String fpl = br.readLine();
			if (fpl.startsWith("FPSICS-1.0")) {
				// OK
			} else {
				E.error("cant read " + fpl);
				OK = false;
			}

			if (OK) {
				String brt = br.readLine();
				String sncom = br.readLine();
				ncommand = readInt(sncom);
				String snch = br.readLine();
				int nchannel = readInt(snch);

				for (int i = 0; i < nchannel; i++) {
					PPPChannel ch = new PPPChannel();
					ch.populateFrom(br);
				}
			}

			if (OK) {
				int nsyn = readInt(br.readLine());
				for (int i = 0; i < nsyn; i++) {
					PPPSynapse sy = new PPPSynapse();
					sy.populateFrom(br);
				}
				
			}
			
			int nsypop = readInt(br.readLine());
			br.readLine();
			
			
			if (OK) {
				pppCell = new PPPCell();
				pppCell.populateFrom(br);
			}


			if (OK) {
				String sncr = br.readLine();
				int [] wl = readInts(sncr, 3);
				int nclamp = wl[0];
				int nrecorder = wl[1];
				int sepFiles = wl[2];
				String ssave = br.readLine();
				if (sepFiles == 1) {
					String sfext = br.readLine();
				}
				
				for (int i = 0; i < nclamp; i++) {
					PPPClamp clamp = new PPPClamp();
					clamp.populateFrom(br);
					clamp.setPosition(pppCell.getPosition(clamp.getTarget()));
					clamps.add(clamp);
				}

				for (int i = 0; i < nrecorder; i++) {
					PPPRecorder rec = new PPPRecorder();
					rec.populateFrom(br);
					rec.setPosition(pppCell.getPosition(rec.getTarget()));
					recorders.add(rec);
				}
			}

			if (OK) {
				int neg = readInt(br.readLine());
				for (int i = 0; i < neg; i++) {
					PPPEventGenerator peg = new PPPEventGenerator();
					peg.populateFrom(br);
				}
				
			}
			
			
			if (OK) {
				String send = br.readLine();
				if (send.startsWith("END")) {
					// OK
				} else {
					E.error("PPP misread " + send);
				}
			}

		} catch (IOException ex) {
			E.error("" + ex);
		}
	}



	public ArrayList<PPPClamp> getClamps() {
		return clamps;
	}

	public ArrayList<PPPRecorder> getRecorders() {
		return recorders;
	}




	public static int readInt(String s) {
		StringTokenizer st = new StringTokenizer(s, " \t");
		String tok = st.nextToken();
		int ret = Integer.parseInt(tok);
		return ret;
	}


	public static int[] readInts(String s, int n) {
		int[] ret = new int[n];
		StringTokenizer st = new StringTokenizer(s, " \t");
		for (int i = 0; i < n; i++) {
			ret[i] = Integer.parseInt(st.nextToken());
		}
		return ret;
	}

	public static double[] readDoubles(String s, int n) {
		double[] ret = new double[n];
		StringTokenizer st = new StringTokenizer(s, " \t");
		for (int i = 0; i < n; i++) {
			ret[i] = Double.parseDouble(st.nextToken());
		}
		return ret;
	}


	public double[][] getComparmentPositionArrays() {
		return pppCell.getCompartmentPositionArrays();
	}
}
