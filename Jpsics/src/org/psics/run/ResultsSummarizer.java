package org.psics.run;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import org.catacomb.dataview.Plot;
import org.psics.be.E;
import org.psics.env.Version;
import org.psics.model.control.PSICSRun;
import org.psics.model.display.BaseGraph;
import org.psics.model.display.Raster;
import org.psics.model.display.ViewConfig;
import org.psics.num.CalcSetSummary;
import org.psics.num.CalcSummary;
import org.psics.read.FileModelSource;
import org.psics.read.ModelSource;
import org.psics.util.FileUtil;
import org.psics.util.TimeUtil;
import org.psics.xml.XMLReader;

public class ResultsSummarizer {


	XMLReader xmlr;


	private void makeVCImages(File f, ViewConfig vc) {
		for (BaseGraph bg : vc.getGraphs()) {
			ViewMaker vm = new ViewMaker(bg);
			vm.makeImages(f);
		}
		for (Raster ras : vc.getRasters()) {
			RasterMaker rm = new RasterMaker(ras);
			rm.makeImage(f);
		}
		// E.info("made images");
	}


	public void makeImages(File fdir, ArrayList<ViewConfig> viewConfigs) {
		for (ViewConfig  vc : viewConfigs) {
			makeVCImages(fdir, vc);
		}
	}


	public String buildSummary(File fdir, String modelName, boolean showMorph, boolean showMorphData) {
		StringBuffer sb = new StringBuffer();

		CalcSetSummary css = getSummaries(fdir);

		File f = new File(fdir, modelName);

		ModelSource mSource = new FileModelSource(f);
		Object objr = mSource.simpleRead();
		// E.info("simple read of " + modelName + " produced " + objr);
		PSICSRun pr = (PSICSRun) objr;

		sb.append("<h2>" + fdir.getName() + " (" + modelName + ")</h2>");
		sb.append("<p><b><span id=\"info\">" + pr.getInfo() + "</span></b></p>\n");

		for (String spara : pr.getInfoParas()) {
			sb.append("<p>");
			sb.append(spara);
			sb.append("</p>");
		}

		File frm = new File(fdir, "README");
		if (frm.exists()) {
			String s = FileUtil.readStringFromFile(frm);
			s = s.replace("-p-", "</p><p>");
			sb.append("<p>\n" + s + "</p>\n");
		}


		sb.append(makeMultiRunSummary(fdir, css));

		if (showMorph) {
			sb.append("<h4>Morphology: " + pr.getMorphID() + "</h4>\n");
		} 
		if (showMorph || showMorphData) {
			MorphPlot mp = new MorphPlot(pr, css);
			mp.makeImages(fdir, sb, showMorph, showMorphData);
		}

		ArrayList<String> imnm = pr.getOutputImageNames();
		if (imnm.size() > 0) {
			sb.append("<h3>Predefined views</h3>\n");
			for (String sim : pr.getOutputImageNames()) {
				sb.append("<h4>" + sim + "</h4>\n");
				sb.append("<div><img class=\"plot\" src=\"" + sim + ".png\"/></div>");

				File finfo = new File(fdir, sim + ".txt");
				if (finfo.exists()) {
					sb.append("<div>");
					sb.append(FileUtil.readStringFromFile(finfo));
					sb.append("</div>\n");
				} else {
					// E.info("no such file " + finfo.getAbsolutePath());
				}


			}
		}


		sb.append("<h2>All files</h2>");
		sb.append("<table class=\"files\"><tr><td>Model</td><td>Preprocessed</td>");
		sb.append("<td>Outupt data</td><td>Reference data etc</td></tr>\n");

		StringBuffer sbm = new StringBuffer();
		StringBuffer sbp = new StringBuffer();
		StringBuffer sbd = new StringBuffer();
		StringBuffer sbr = new StringBuffer();

		ArrayList<File> tojar = new ArrayList<File>();

		for (File ff : fdir.listFiles()) {
			String sl = ("<a href=\"" + ff.getName() + "\">" + ff.getName() + "</a><br>\n");
			String fnm = ff.getName();
			if (ff.isDirectory()) {
				// skip it;

			} else if (fnm.endsWith(".xml")) {
				if (fnm.endsWith("view.xml")) {
					// shouldn't be there at all

				} else {
					sbm.append(sl);
					tojar.add(ff);
				}

			} else if (fnm.endsWith(".sum") || fnm.endsWith("log.txt")) {
				sbd.append(sl);


			} else if (fnm.endsWith(".ppp")) {
				sbp.append(sl);

			} else if (fnm.endsWith(".txt")) {
				File fdat = FileUtil.getSiblingFile(ff, ".dat");
				if (fdat.exists()) {
					sbd.append(sl);
				} else {
					sbr.append(sl);
				}


			} else if (fnm.endsWith(".dat")) {
				sbd.append(sl);


			} else if (fnm.endsWith(".png") || fnm.endsWith(".out")
					|| fnm.endsWith(".html") || fnm.endsWith(".css") || fnm.endsWith(".jar")) {
				// skip images, generated html, and css

			} else {
				sbr.append(sl);
			}
		}

		sb.append("<tr>");
		sb.append("<td valign=\"top\">" + sbm.toString() + "</td>\n");
		sb.append("<td valign=\"top\">" + sbp.toString() + "</td>\n");
		sb.append("<td valign=\"top\">" + sbd.toString() + "</td>\n");
		sb.append("<td valign=\"top\">" + sbr.toString() + "</td>\n");
		sb.append("</tr>");
		sb.append("</table>");
		sb.append("<h2>Model</h2>");

		String jarname = fdir.getName() + ".jar";
		HashMap<String, String> atthm = new HashMap<String, String>();
		atthm.put("MASTER", modelName);
		atthm.put("PSICSVERSION", Version.getVersionName());

		FileUtil.writeJarFile(tojar, new File(fdir, jarname), atthm);
		sb.append("Archive file of the complete model: <a href=\"" + jarname + "\">" + jarname + "</a><br/>\n");


		for (File ff : fdir.listFiles()) {
			String fnm = ff.getName();
			if (fnm.endsWith(".xml")) {
				if (fnm.endsWith("summary.xml")) {
					// skip it
				} else if (fnm.endsWith("view.xml")) {
					// also skip
				} else {
					writeModelFile(ff, sb);
				}

			}
		}
		return sb.toString();
	}




	private CalcSetSummary getSummaries(File fdir) {
		CalcSetSummary css = new CalcSetSummary();
		XMLReader axmlr = new XMLReader();
		axmlr.addSearchPackage(CalcSetSummary.class.getPackage());

		File flog = new File(fdir, "log.txt");
		String slog = FileUtil.readStringFromFile(flog);
		StringTokenizer st = new StringTokenizer(slog, "\n");
		while (st.hasMoreTokens()) {
			String slin = st.nextToken().trim();
			if (slin.startsWith("ppp:")) {
				StringTokenizer lst = new StringTokenizer(slin);
				lst.nextToken();
				String fpat = lst.nextToken();
				String scpu = lst.nextToken();

				File fsum = new File(fdir, fpat + ".sum");
				if (fsum.exists()) {
					try {
					String ssum = FileUtil.readStringFromFile(fsum);
					CalcSummary cs = (CalcSummary)(axmlr.read(ssum));
					css.add(cs);
					cs.setCPUTime(Double.parseDouble(scpu));
					} catch (Exception ex) {
						E.error("" + ex);
					}
					} else {
					E.error("log mentions " + fpat + " but no summary file?");
				}
			}
		}
		return css;
	}





	private String makeMultiRunSummary(File fdir, CalcSetSummary css) {
		int imgcount = 0;
		StringBuffer sbsum = new StringBuffer();



		double cpuTotal = 0.;

		StringBuffer sbtbl = new StringBuffer();
		sbtbl.append("<table class=\"bmres\" cellspacing=\"0\" cellpadding=\"2\">\n");

		int nsum = css.nSummaries();

		String ylabel = "CPU Time / s";
		String xlabel = "";

		{
			CalcSummary cs = css.getOne();

			sbtbl.append("<tr class=\"head\">");
			addHCell(sbtbl, "Compartments");

			addHCell(sbtbl, "Stochastic<br/>channels / cpmts");
			addHCell(sbtbl, "Continuous<br/>channels / cpmts");
			addHCell(sbtbl, "Non Gated<br/>channels / cpmts");


			if (cs.variableName != null) {
				if (!cs.variableName.equals("runTime")) {
					addHCell(sbtbl, "time/ms");
				}
				if (!cs.variableName.equals("timeStep")) {
					addHCell(sbtbl, "timestep/ms");
				}

				if (cs.variableName.length() > 0) {
					addHCell(sbtbl, "<b>" + cs.variableName + "</b>");
					xlabel = getPlottableName(cs.variableName);
				}
			}

			addHCell(sbtbl, "CPU Time / s");
		}


		int iset = 0;
		double[] xpts = new double[nsum];
		double[] ypts = new double[nsum];

		for (CalcSummary cs : css.getCalcSummaries()) {
			sbtbl.append("<tr>\n");


			addCell(sbtbl, cs.ncompartments);

			addCell(sbtbl, cs.nchannels_stoch + " / " + cs.ncompartments_stoch);
			addCell(sbtbl, cs.nchannels_cont + " / " + cs.ncompartments_cont);
			addCell(sbtbl, cs.nchannels_ng + " / " + cs.ncompartments_ng);



			if (cs.variableName != null) {
				if (!cs.variableName.equals("runTime")) {
					addCell(sbtbl, cs.runtime);
				}
				if (!cs.variableName.equals("timeStep")) {
					addCell(sbtbl, cs.dt);
				}

				if (cs.variableName.length() > 0) {
					addCell(sbtbl, cs.variableText, "ca hlt");
				}
			}

			addCell(sbtbl, cs.cputime, "hlt la");
			cpuTotal += cs.cputime;

			ypts[iset] = cs.cputime;

			if (cs.variableName != null) {
				xpts[iset] = getPlottableQuantity(cs.variableName, cs, cs.variableValue);
			}
			iset += 1;

			sbtbl.append("</tr>\n");
		}
		sbtbl.append("</table>\n");

		Plot p = new Plot();
		p.addSortedLine(xpts, ypts, Color.white);
		p.addPoints(xpts, ypts, Color.white);
		p.setXLabel(xlabel);
		p.setYLabel(ylabel);
		File fim = new File(fdir, "sml-" + imgcount + ".png");
		imgcount += 1;

		p.makeImage(360, 220, fim);

		sbsum.append(sbtbl.toString());

		sbsum.append("<table class=\"cpuimg\"><tr><td><img src=\"" + fim.getName() + "\"/></td></tr><tr><td>");
		sbsum.append("</td></tr></table>\n");


		StringBuffer sbpage = new StringBuffer();
		sbpage.append("<p>Total CPU time <b><span id=\"cputime\">" + String.format("%8.4g", cpuTotal)
				+ "</span></b> seconds;");
		sbpage.append(" at " + TimeUtil.timeDateStamp() + "</p>");
		sbpage.append(sbsum.toString());

		return sbpage.toString();


	}


	private double getPlottableQuantity(String vnm, CalcSummary cs, double val) {
		double ret = val;
		if (vnm.equals("timeStep")) {
			ret = 1. / (cs.dt);
		} else if (vnm.equals("baseElementSize")) {
			ret = cs.ncompartments;
		}
		return ret;
	}


	private String getPlottableName(String vnm) {
		String ret = vnm;
		if (vnm.equals("timeStep")) {
			ret = "steps per ms";
		} else if (vnm.equals("baseElementSize")) {
			ret = "No of compartments";
		}
		return ret;
	}


	private void addHCell(StringBuffer sb, String txt) {
		sb.append("<td>");
		sb.append(txt);
		sb.append("</td>");
	}


	private void addCell(StringBuffer sb, double d) {
		addCell(sb, d, "");
	}


	private void addCell(StringBuffer sb, int n) {
		addCell(sb, "" + n);
	}


	private void addCell(StringBuffer sb, double d, String cls) {
		String sd = "" + d;
		if (sd.length() > 5) {
			sd = String.format("%10.3g", d);
		}
		addCell(sb, sd, cls);

	}


	private void addCell(StringBuffer sb, String txt) {
		addCell(sb, txt, "");
	}


	private void addCell(StringBuffer sb, String txt, String cls) {
		if (cls != null && cls.length() > 0) {
			sb.append("<td class=\"" + cls + "\"/>");
		} else {
			sb.append("<td class=\"ca\">");
		}
		sb.append(txt);
		sb.append("</td>");
	}


	private static void writeModelFile(File f, StringBuffer sb) {
		String stxt = FileUtil.readStringFromFile(f);
		StringTokenizer st = new StringTokenizer(stxt, "\n");
		int nl = st.countTokens();
		if (nl > 100) {
			sb.append("<h3>" + f.getName() + " (truncated)</h3>");
			sb.append("<div class=\"model\">\n<pre>");

			for (int i = 0; i < 60; i++) {
				String line = st.nextToken();
				line = line.replaceAll("&", "&amp;");
				line = line.replaceAll("<", "&lt;");
				line = line.replaceAll(">", "&gt;");
				sb.append(line);
				sb.append("\n");
			}
			sb.append("\n - and " + (nl - 60) + " more lines -");
			sb.append("</pre></div>\n");


		} else {
			sb.append("<h3>" + f.getName() + "</h3>");
			sb.append("<div class=\"model\">\n<pre>");

			stxt = stxt.replaceAll("&", "&amp;");
			stxt = stxt.replaceAll("<", "&lt;");
			stxt = stxt.replaceAll(">", "&gt;");
			sb.append(stxt);
			sb.append("</pre></div>\n");
		}


	}
}
