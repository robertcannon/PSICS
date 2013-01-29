package org.psics.run;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;

import org.catacomb.dataview.Plot;
import org.catacomb.dataview.display.PolyLine;
import org.catacomb.numeric.data.AsciiIO;
import org.catacomb.numeric.data.DataTable;
import org.psics.be.E;
import org.catacomb.util.ColorUtil;
import org.psics.model.display.BaseLineSet;
import org.psics.model.display.DataComparison;
import org.psics.model.display.Stats;
import org.psics.model.display.LineGraph;
import org.psics.model.display.LineSet;
import org.psics.model.display.MeanVariance;
import org.psics.model.display.PowerSpectrum;
import org.psics.num.math.Array;
import org.psics.num.math.FourierTransform;
import org.psics.num.math.Interpolator;
import org.psics.util.FileUtil;


public class LineGraphPlotter {

	public static ArrayList<String>  populatePlot(LineGraph graph, File dir, Plot p) {

		ArrayList<String> ret = new ArrayList<String>();
		for (BaseLineSet blset : graph.getBaseLineSets()) {
			Color c = ColorUtil.parseColor(blset.getColor());

			File f = new File(dir, blset.getFileName());

			// bit of a hack
			if (!f.exists()) {
				File ftry = new File(f.getParentFile().getParentFile(), f.getName());
				if (ftry.exists()) {
					FileUtil.copyFile(ftry, f);
				}
			}

			DataTable tbl = null;
			if (f.exists()) {
			String s = FileUtil.readFirstLine(f);
			if (s.indexOf("FPSICS2") >= 0) {
				tbl = PSICSDataReader.readTable(f);
			} else {
				tbl = AsciiIO.readTable(f);
			}

			 if (tbl != null) {
			 if (blset instanceof LineSet) {
				 ArrayList<String> als = addLines((LineSet)blset, tbl, c, p, dir, blset.getComparisons());
				 ret.addAll(als);

				 ArrayList<String> alstat = addStats((LineSet)blset, tbl, blset.getStats());
				 ret.addAll(alstat);



			 } else if (blset instanceof PowerSpectrum) {
				 addPowerSpectrum((PowerSpectrum)blset, tbl, c, p);

			 } else if (blset instanceof MeanVariance) {
				 ArrayList<String> als = addMeanVariance((MeanVariance)blset, tbl, c, p);
				 ret.addAll(als);

			 } else {
				 E.missing("unrecognized type " + blset);
			 }
			 } else {
				 E.warning("cant read data from " + f);
			 }
			} else {
				E.warning("no such file: " + f + " " + f.getAbsolutePath());
			}
		}
		return ret;
	}



	private static void addPowerSpectrum(PowerSpectrum pspec, DataTable tbl, Color c, Plot p) {
		 double[] t = tbl.getColumn(0);
		 double[] v = tbl.getColumn(1);

		 double dt = (t[t.length -1] - t[0]) / t.length;

		 FourierTransform ftrans = new FourierTransform();
		 ftrans.setData(v);
		 ftrans.setDeltaT(0.001 * dt);


		 ftrans.setSegmentLength(4096);
		 ftrans.setOverlapOption(true);


		 double[][] ddat = ftrans.powerSpectrum();

		 int nd = ddat[1].length;
		 double[] scdat = new double[nd];
		 double f = dt * 1000.;
		 for (int i = 0; i < nd; i++) {
			 scdat[i] = f * ddat[1][i];
		 }
		 if (pspec.isLogLog()) {
			double[] lt = new double[nd];
			double[] lsc = new double[nd];
			for (int i = 0; i < nd; i++) {
				lt[i] = (ddat[0][i] <= 0. ? 0. : Math.log10(ddat[0][i]));
				lsc[i] = (scdat[i] <= 0 ? 0. : Math.log10(scdat[i]));
				p.addLine(lt, lsc, c);
			}



		 } else {
			 p.addLine(ddat[0], scdat, c);
		 }
	}






	private static ArrayList<String> addMeanVariance(MeanVariance mvar, DataTable tbl, Color c, Plot p) {
		 ArrayList<String> ret = new ArrayList<String>();
		 double[] t = tbl.getColumn(0);

		 double[][] dat = tbl.getRows();

		 double[] rng = mvar.getRange();
		 double tmin = rng[0];
		 double tmax = rng[1];
		 if (tmax <= tmin) {
			 tmax = t[t.length - 1];
		 }

		 int imin = -1;
		 int imax = -1;
		 for (int i = 0; i < t.length; i++) {
			 if (imin < 0. && t[i] > tmin) {
				 imin = i;
			 }
			 if (imax < 0 && t[i] > tmax) {
				 imax = i - 1;
			 }
		 }
		 if (imin < 0) {
			 imin = 0;
		 }
		 if (imax < 0) {
			 imax = t.length-1;
		 }


		 int ncols = tbl.getNColumn() - 1;
		 int ni = imax - imin + 1;

		 double[] mean = new double[ni];
		 double [] var = new double[ni];


		 int nsub = 4;
		 double[][] smean = new double[nsub][ni];
		 double[][] svar = new double[nsub][ni];
		 int[] npl = new int[nsub];


		 for (int i = 0; i < ni; i++) {
			 for (int j = 0; j < ncols; j++) {
				 double dij = -dat[imin + i][j+1];
				 mean[i] += dij;
				 var[i] += dij * dij;
				 int ini = j % nsub;
				 if (i == 0) {
					 npl[ini] += 1;
				 }
				 smean[ini][i] += dij;
				 svar[ini][i] += dij * dij;

			 }
			 mean[i] /= ncols;
			 var[i] /= ncols;
			 var[i] -= mean[i] * mean[i];
		 }
		 for (int k = 0; k < nsub; k++) {
			 for (int i = 0; i < ni; i++) {
				 smean[k][i] /= npl[k];
				 svar[k][i] /= npl[k];
				 svar[k][i] -= smean[k][i] * smean[k][i];
			 }
		 }


		 double[] xp = null;
		 double[] yp = null;

		 double b = mvar.getBinSize();
		 if (b <= 0.) {
			 xp = mean;
			 yp = var;

		 } else {
			 double mx = Array.max(mean);

			 int np = (int)(mx / b + 1);
			 double[] mp = new double[np];
			 double[] vp = new double[np];
			 int[] nn = new int[np];
			 for (int i = 0; i < mean.length; i++) {
				 int iv = (int)(mean[i] / b);
				 if (iv >= 0 && iv < np) {
				 vp[iv] += var[i];
				 nn[iv] += 1;
				 } else {
					 E.warning("mean out of range? " + mean[i]);
				 }
			 }
			 int nnz = 0;
			 for (int i = 0; i < np; i++) {
				 mp[i] = (i + 0.5) * b;
				 if (nn[i] > 0) {
					 nnz += 1;
					 vp[i] /= nn[i];
				 }
			 }
			   xp = new double[nnz];
			   yp = new double[nnz];
			 nnz = 0;
			 for (int i = 0; i < np; i++) {
				 if (nn[i] > 0) {
					 xp[nnz] = mp[i];
					 yp[nnz] = vp[i];
					 nnz += 1;
				 }
			 }

		 }
		 p.addPoints(xp, yp, c);

		 for (int k = 0; k < smean.length; k++) {
			 p.addSmallPoints(smean[k], svar[k], c);
		 }


		 if (mvar.hasComparision()) {
			 int nch = mvar.getComparisonNChannel();
			 double g = mvar.getComparisonGSingle();

			 double err = 0.;
			 double err2 = 0.;
			 double[] cp = new double[xp.length];
			 for (int i = 0; i < xp.length; i++) {
				 double pop = (xp[i] / g) / nch;
				 cp[i] = nch * pop * (1. - pop) * g * g;

				 double derr = yp[i] - cp[i];
				 err += derr;
				 err2 += derr * derr;
			 }
			 err /= xp.length;
			 err2 /= xp.length;
			 double rms = Math.sqrt(err2);

			 E.info("avg and rms errors: " + err + " " + rms);

			 String smsg = "Deviation for " + tbl.getID() +
			 	" range=(" + tmin + ", " + tmax + ") nch=" + nch + " i=" + g +
			 	" abs err=" + String.format("%10.3g", err) + " rms err=" + String.format("%10.3g", rms);
			 ret.add(smsg);

			 p.addLine(xp, cp, Color.white);
		 }

		 return ret;
	}







	private static ArrayList<String> addLines(LineSet lset, DataTable tbl, Color c, Plot p,
											  File fdir, ArrayList<DataComparison> comps) {
			 ArrayList<String> ret = new ArrayList<String>();

	         int ncol = tbl.getNColumn();
	         int ncolp = ncol;
	         double[] sf = getScaleFactors(lset.getRescaling(), ncol);

	         int nm = lset.getMaxshow();
	         if (nm > 0 && ncol > nm + 1) {
	        	 ncolp = nm + 1;
	         }

	         double[] xpts = scaleColumn(tbl.getColumn(0), sf[0]);

 	         String fun = lset.getFunction();


	         int ishow = lset.getShow();
	         if (ishow > 0) {
	        	 addOneLine(lset, tbl, ishow, sf, xpts,p, c, comps, fdir, ret);
	         } else {

	        	 if (fun == null || fun.trim().length() == 0) {
	        	// E.info("adding " + ncol + " lines ");
	        	 for (int i = 1; i < ncolp; i++) {
	        		 addOneLine(lset, tbl, i, sf, xpts,p, c, comps, fdir, ret);
	        	 }

	        	 } else if (fun.equals("mean")) {
	        		 double[] ypts = new double[xpts.length];
	            	 for (int i = 1; i < ncol; i++) {
	            		 double[] co = scaleColumn(tbl.getColumn(i), sf[i]);
	            		 for (int j = 0; j < xpts.length; j++) {
	            			 ypts[j] += co[j];
	            		 }
	            	 }
	            	 for (int j = 0; j < xpts.length; j++) {
	        			 ypts[j] /= (ncol - 1);
	        		 }
	            	 String sleg = null;
	            	 sleg = lset.getLabel();
	        		 if (sleg == null) {
	        			 sleg = lset.getFileName();
	        		 }
	            	 sleg += "-" + fun;

	            	  p.addLine(xpts, ypts, c, sleg);



	            	  if (comps != null) {
	            		 	for (DataComparison dc : comps) {
	            		 		ret.add(tbl.getID() + ", line " + dc.getLine() + ", " +
	            		 					writeComparison(fdir, dc, xpts, ypts));
	            		 	}
	            	 	}




	        	 } else {

	        		 E.error("unrecognized value (only 'mean' allowed) for function: " + fun);
	        	 }

	         }




	         return ret;
	}




	private static ArrayList<String> addStats(LineSet lset, DataTable tbl,
			ArrayList<Stats> comps) {
		ArrayList<String> ret = new ArrayList<String>();

		int ncol = tbl.getNColumn();
		double[] sf = getScaleFactors(lset.getRescaling(), ncol);

		int nm = lset.getMaxshow();
		if (nm > 0 && ncol > nm + 1) {
			ncol = nm + 1;
		}

		double[] xpts = scaleColumn(tbl.getColumn(0), sf[0]);

		int ishow = lset.getShow();
		if (ishow > 0) {
			addOneStats(lset, tbl, ishow, sf, xpts, comps, ret);
		} else {
			for (int i = 1; i < ncol; i++) {
				addOneStats(lset, tbl, i, sf, xpts, comps, ret);
			}
		}
		return ret;
	}






	 private static void addOneStats(LineSet lset, DataTable tbl, int i, double[] sf, double[] xpts,
			 ArrayList<Stats> stats,  ArrayList<String> ret) {

	String sleg = null;
	 if (i == 1) {
		 sleg = lset.getLabel();
		 if (sleg == null) {
			 sleg = lset.getFileName();
		 }
	 } else {
		 sleg = null;
	 }
	 double[] y = scaleColumn(tbl.getColumn(i), sf[i]);
 //   p.addLine(xpts, y, c, sleg);


    if (stats != null) {
	 	for (Stats st : stats) {
	 			double[] msd = getMeanSD(xpts, y, st.getXMin(), st.getXMax());
	 			ret.add(String.format("mean= %10.4g,     sd= %10.4g       ", msd[0], msd[1]) +
	 					tbl.getID() + ", line " + i);

	 	}
	}
	 }






	 private static double[] getMeanSD(double[] xpts, double[] y, double minin, double maxin) {
		 double max = maxin;
		 double min = minin;
		 if (Double.isNaN(min)) {
			 min = xpts[0] - 1;
		 }
		 if (Double.isNaN(max)) {
			 max = xpts[xpts.length-1] + 1;
		 }
		 int np = 0;
		 double  sum = 0.;
		 double sumsq = 0.;
		 for (int i = 0; i < xpts.length; i++) {
			 if (xpts[i] > min && xpts[i] < max) {
				 sum += y[i];
				 sumsq += y[i] * y[i];
				 np += 1;
			 }
		 }
		 double mu = sum / np;
		 double var = sumsq / np - mu * mu;
		 double sd = (var > 0. ? Math.sqrt(var) : 0.);
		 double[] ret = {mu, sd};

		 E.info("got msd for " + np + " " + mu + " "+ sd);
		 return ret;
	}





	private static void addOneLine(LineSet lset, DataTable tbl, int i, double[] sf, double[] xpts,
			 Plot p, Color c,
			 ArrayList<DataComparison> comps, File fdir, ArrayList<String> ret) {

	String sleg = null;
	 if (i == 1) {
		 sleg = lset.getLabel();
		 if (sleg == null) {
			 sleg = lset.getFileName();
		 }
	 } else {
		 sleg = null;
	 }
	 double[] y = scaleColumn(tbl.getColumn(i), sf[i]);
     p.addLine(xpts, y, c, sleg);
    // E.info("added line with legend " + sleg + " " + i);


     if (comps != null) {
	 	for (DataComparison dc : comps) {
	 		if (dc.getLine() == i - 1) {
	 			ret.add(tbl.getID() + ", line " + dc.getLine() + ", " +
	 					writeComparison(fdir, dc, xpts, y));

		 	}
	 	}
 	}
	 }


	private static String writeComparison(File fdir, DataComparison dc, double[] xpts, double[] y) {

		File f = new File(fdir, dc.getFileName());
		DataTable rtbl = AsciiIO.readTable(f);
		double[] sf = getScaleFactors(dc.getRescaling(), 2);
		double[] datx = scaleColumn(rtbl.getColumn(0), sf[0]);
		double[] daty = scaleColumn(rtbl.getColumn(1), sf[1]);

		double[] mvar = calcMeanVarDeviation(xpts, y, datx, daty);

		E.info("mean and sd: " + mvar[0] + " " + mvar[1]);
		String ret = String.format("deviation: mean =%14.3g, rms=%14.3g", mvar[0], mvar[1]);
		return ret;
	}



	private static double[] calcMeanVarDeviation(double[] x, double[] y, double[] refx, double[] refy) {
		double mean = 0.;
		double var = 0.;

		int n = 0;
		Interpolator interp = new Interpolator(refx, refy);
		for (int i = 0; i < x.length; i++) {
			n += 1;
			double rv = interp.valueAt(x[i]);
			double dev = y[i] - rv;
			mean += dev;
			var += dev * dev;
		}
		mean /= n;
		var /= n;

		double[] ret = {mean, Math.sqrt(var)};
		return ret;
	}







	public static double[] getScaleFactors(String rescale, int nsf) {
	      double[] ret = new double[nsf];
	      for (int i = 0; i < nsf; i++) {
	         ret[i] = 1.;
	      }
	      if (rescale == null) {
	          // leave as is;

	      } else {
	         double[] row = AsciiIO.readRow(rescale);
	         if (row == null) {
	            E.warning("cant read scale factors from " + rescale);
	         } else {
	            for (int i = 0; i < nsf && i < row.length; i++) {
	               ret[i] = row[i];
	            }

	         }
	      }
	         return ret;
	   }


	   protected static double[] scaleColumn(double[] d, double sf) {
	      double[] ret = new double[d.length];
	      for (int i = 0; i < d.length; i++) {
	         ret[i] = sf * d[i];
	      }
	      return ret;
	   }
}
