package org.psics.run;

import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.psics.model.display.ViewConfig;
import org.psics.util.FileUtil;
import org.psics.util.JUtil;

public class Reporter {




	public static void main(String[] argv) {
		File fdir = new File(System.getProperty("user.dir"));
		mkdocs(fdir);
	}



	public static void mkdocs(File dir) {
		ResultsSummarizer rs = new ResultsSummarizer();

		String sl = FileUtil.readStringFromFile(new File(dir, "log.txt"));

		StringTokenizer st = new StringTokenizer(sl);
		String rname = st.nextToken();

		File fmod = new File(dir, rname);
		PSICSModel pm = new PSICSModel(fmod);

		ArrayList<ViewConfig> avc = pm.getViewConfigs();
		rs.makeImages(dir, avc);

		// TODO - could respect independent settings fir different view configs
		boolean sm = true;
		boolean smd = false;
		for (ViewConfig vc : avc) {
			if (!vc.showMorph()) {
				sm = false;
			}
			if (vc.showMorphologyData()) {
				smd = true;
			}
		}
		
		

		String stxt = rs.buildSummary(dir, rname, sm, smd);
		String[] sa = makeHeaderFooter("../", "PSICS Results for " + rname);
		writeWrapped(stxt, sa, new File(dir, "index.html"));

		JUtil.copyResource(new Reporter(), "report.css", dir);

	}



	public static void mkindex(File dir) {
		Indexer idx = new Indexer(dir);
		idx.buildIndex(makeHeaderFooter("", "PSICS Results index for " + dir.getName()));
		JUtil.copyResource(new Reporter(), "report.css", dir);
	}



	private static void writeWrapped(String stxt, String[] sa, File fout) {
		StringBuffer sb = new StringBuffer();
		sb.append(sa[0]);
		sb.append(stxt);
		sb.append(sa[1]);
		FileUtil.writeStringToFile(sb.toString(), fout);
	}



	private static String[] makeHeaderFooter(String uppath, String ttl) {
		 StringBuffer sb = new StringBuffer();
		 sb.append("<html><head>\n");
		 sb.append("<title>" + ttl + "</title>\n");
		 sb.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"report.css\"/>\n");
		 sb.append("</head>\n");
		 sb.append("<body>\n");
		 sb.append("<div class=\"banner\">pSICS Results summary</div>\n");

		 sb.append("<div class=\"menu\">\n");
		 sb.append("<a href=\"" + uppath + "index.html\">Index</a>");
		 sb.append("</div>\n");

		 sb.append("<div class=\"index\">\n");
		 sb.append("</div>\n");

		 sb.append("<div class=\"content\">\n");

		 String[] ret = new String[2];
		 ret[0] = sb.toString();
		 ret[1] = "</div>\n</body>\n</html>\n";
		 return ret;
	}

}
