package org.psics.run;
import org.psics.ppp.PPP;
import org.psics.ppp.PPPClamp;
import org.psics.ppp.PPPEventGenerator;
import org.psics.ppp.PPPRecorder;
import org.psics.util.FileUtil;


import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.catacomb.dataview.Plot;
import org.psics.be.E;
import org.psics.model.control.PSICSRun;
import org.psics.num.CalcSetSummary;
import org.psics.num.CalcSummary;

public class MorphPlot {

	PSICSRun prun;
	CalcSetSummary css;

	public MorphPlot(PSICSRun pr, CalcSetSummary cs) {
		prun = pr;
		css = cs;
	}

	public void makeImages(File fdir, StringBuffer sb, boolean showMorph, boolean showMorphData) {
		int ics = 0;
		for (CalcSummary  cs : css.getCalcSummaries()) {
			ics += 1;
			File fppp = new File(fdir, cs.getFileRoot() + ".ppp");
			PPP ppp = new PPP();
			ppp.populateFrom(fppp);

			File fm = new File(fdir, cs.getFileRoot() + ".out");
			if (fm.exists()) {

				double[][][] regions = readRegions(fm);
				double[][] axyz = ppp.getComparmentPositionArrays();

				StringBuffer sbdat = new StringBuffer();
				if (showMorphData) {
					for (int i = 0; i < regions.length; i++) {
						double[] x = regions[i][0];
						double[] y = regions[i][1];
						double[] z = regions[i][2];
						
						sbdat.append("xyzline region-" + i + " " + x.length + " ");
						for (int j = 0; j < x.length; j++) {
							sbdat.append(String.format(" %8.3f %8.3f %8.3f ", x[j], y[j], z[j]));
						}
						sbdat.append("\n");
						
					}
					
					for (int i = 0; i < axyz[0].length; i++) {
						sbdat.append("point compartment-" + i + " 3 ");
						sbdat.append(String.format(" %8.3f %8.3f %8.3f", axyz[0][i], axyz[1][i], axyz[2][i]));
						sbdat.append("\n");
					}
				}
			
				Plot p = new Plot();
				for (double[][] da : regions) {
					p.addPolygon(da[0], da[1], Color.black);
				}
				p.addDots(axyz[0], axyz[1], Color.yellow);


				for (PPPClamp pc : ppp.getClamps()) {
					double[] xyz = pc.getPosition();
					String sc = "" + pc.getClampTypeString() +
						" clamp " + pc.getID();
					p.addArrowString(xyz[0], xyz[1], Color.red, sc);
					if (showMorphData) {
						sbdat.append("point " + pc.getClampTypeString() + "-clamp-" + pc.getID() + " 3 " +
								String.format("%8.3f %8.3f", xyz[0], xyz[1]));
						sbdat.append("\n");
					}
				}

				for (PPPRecorder pr : ppp.getRecorders()) {
					double[] xyz = pr.getPosition();
					String sc = "" + pr.getRecorderTypeString() +
						" rec " + pr.getID();
					p.addArrowString(xyz[0], xyz[1], Color.green, sc);
					if (showMorphData) {
						sbdat.append("point " + pr.getRecorderTypeString() + "-rec-" + pr.getID() + " 3 " +
								String.format("%8.3f %8.3f", xyz[0], xyz[1]));
						sbdat.append("\n");
					}
				}

				if (showMorphData) {
					File fdat = new File(fdir, cs.getFileRoot() + ".morph");
					FileUtil.writeStringToFile(sbdat.toString(), fdat);
				}
				
				if (showMorph) {
				p.autorange(true);
				String imnm = cs.getFileRoot() + ".png";
				File fim = new File(fdir, imnm);
				p.makeImage(550, 500, fim);
				sb.append("<div><img src=\"" + imnm + "\"/></div>\n");
				sb.append("<p>&nbsp;</p>\n");
				}
			}
		}



	}


	public double[][][] readRegions(File f) {
		double[][][] ret = null;
		try {
			BufferedReader br = new BufferedReader(new FileReader(f));

			String s = br.readLine();
			String s2 = br.readLine();
			if (s2.startsWith("volumeGrid")) {
				StringTokenizer st = new StringTokenizer(s2);
				st.nextToken();
				int nel = Integer.parseInt(st.nextToken());
				ret = readMesh(br, nel);

			} else {
				E.error("Cant read " + s2);
			}

		} catch (IOException ex) {
			E.error("cant read " + f + " " + ex);
		}
		return ret;
	}


	   private double[][][] readMesh(BufferedReader br, int nel) {
	      double[][][] mesh = new double[nel][][];
	      try {
	         for (int ilin = 0; ilin < nel; ilin++) {
	               String line = br.readLine();
	               double[] dat = parseLine(line);
	               int n = dat.length / 3;
	               mesh[ilin] = new double[3][n];
	               for (int i = 0; i < n; i++) {
	                  mesh[ilin][0][i] = dat[3 * i];
	                  mesh[ilin][1][i] = dat[3 * i + 1];
	                  mesh[ilin][2][i] = dat[3 * i + 2];
	               }
	         }
	      } catch (Exception ex) {
	         E.error("ex " + ex);
	      }
	      return mesh;
	   }

	   private double[] parseLine(String s) {
	      StringTokenizer st = new StringTokenizer(s, " ()");
	      int n = st.countTokens();

	      double[] ret = new double[n];
	      for (int i = 0; i < n; i++) {
	         ret[i] = Double.parseDouble(st.nextToken());
	      }
	      return ret;
	   }


}
