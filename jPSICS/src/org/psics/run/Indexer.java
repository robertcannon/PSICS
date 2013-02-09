package org.psics.run;

import java.io.File;
 
import org.psics.util.FileUtil;


public class Indexer {

	File rootDir;


	public Indexer(File f) {
		rootDir = f;
	}


	public void buildIndex(String[] headfoot) {
		// ArrayList<String> dirs = new ArrayList<String>();

		StringBuffer xidx = new StringBuffer();
		xidx.append("<ModelIndex>\n");

		StringBuffer sbtop = new StringBuffer();
		StringBuffer sbsub = new StringBuffer();

		for (File f : rootDir.listFiles()) {
			if (f.isDirectory()) {
				 String s = f.getName();
				 sbtop.append("<tr><td><a href=\"" + s + "/index.html\">" + s + "</a></td>\n");
				 File fidx = new File(f, "index.html");
				 if (fidx.exists()) {
				 String stxt = FileUtil.readStringFromFile(fidx);
				 String sinfo = getSpan(stxt, "info");
				 sbtop.append("<td>" + sinfo + "</td>\n");
				 sbtop.append("<td>" + getSpan(stxt, "cputime") + "</td>\n");
				 sbtop.append("</td>\n");
				 sbsub.append("<a href=\"../" + s + "/index.html\">" + s + "</a><br/>\n");

				 xidx.append("<dir name=\"" + s + "\">");
				 xidx.append(sinfo);
				 xidx.append("</dir>\n");
				 }
		    }
		}
		xidx.append("</ModelIndex>\n");


		String sidx = sbsub.toString();

		for (File f : rootDir.listFiles()) {
			if (f.isDirectory()) {
				insertIndex(f, sidx);
			}
		}


		StringBuffer sbidx = new StringBuffer();

		sbidx.append("<h2>Inndex</h2>\n");
		sbidx.append("<p>There is a separate folder for each model containing the model " +
					"specification, results, and basic plots</p>\n");
		sbidx.append("<table class=\"bmres\" cellspacing=\"0\" cellpadding=\"4\">\n");
		sbidx.append("<tr class=\"head\"><td>Folder</td><td>Model</td><td>CPU time</td></tr>\n");

		sbidx.append(sbtop.toString());

		sbidx.append("</table>\n");
		writeWrapped(sbidx.toString(), headfoot, new File(rootDir, "index.html"));

		FileUtil.writeStringToFile(xidx.toString(), new File(rootDir, "index.xml"));
	}



	private static void writeWrapped(String stxt, String[] sa, File fout) {
		StringBuffer sb = new StringBuffer();
		sb.append(sa[0]);
		sb.append(stxt);
		sb.append(sa[1]);
		FileUtil.writeStringToFile(sb.toString(), fout);
	}






	private void insertIndex(File fdir, String sidx) {
		File fpg = new File(fdir, "index.html");
		if (!fpg.exists()) {
			// probably harmless
			// E.warning("no index.html in " + fdir);
			return;
		}

		String stxt = FileUtil.readStringFromFile(fpg);

		String sstart = "<div class=\"index\">";
		String send = "</div>";

		int istart = stxt.indexOf(sstart);
		int iend = stxt.indexOf(send, istart);

		if (istart > 0 && iend > istart) {
			stxt = stxt.substring(0, istart + sstart.length()) + sidx + stxt.substring(iend, stxt.length());

		} else {
			// TODO does this matter? probably not - happens when the destination file is
			// missing anyway because the run failed
			// E.warning("nowhere to insert index in " + fpg + " - looking for " + sstart + "..." + send);
		}


		FileUtil.writeStringToFile(stxt, fpg);
	}



	private String getSpan(String stxt, String id) {
		String ss = "<span id=\"" + id + "\">";
		String se = "</span>";
		String ret = null;
		int istart = stxt.indexOf(ss);
		int iend = stxt.indexOf(se, istart);
		if (istart > 0 && iend > istart) {
			ret = stxt.substring(istart + ss.length(), iend);
		}
		return ret;
	}


}
