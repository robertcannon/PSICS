package org.psics.project;

import java.io.File;
import java.util.StringTokenizer;

import org.psics.be.E;
import org.psics.geom.Ball;
import org.psics.model.morph.MorphologySource;
import org.psics.morph.TreePoint;
import org.psics.util.FileUtil;
import org.psics.util.TimeUtil;


public class StandaloneSWCItem extends StandaloneItem {

    String sourceText;
    String sourceHeader;

	public StandaloneSWCItem(String s, File f) {
		super(s, f, null);
	}


	public String getTypeSummary() {
		return "SWC Morphology";
	}

	public Object getObject() {

		if (object == null) {
			MorphologySource msource = new MorphologySource();
			sourceText = FileUtil.readStringFromFile(file);
			extractHeader();

			msource.setText(sourceText);
			object = msource.getCellMorphology();
		}
		return object;
	}


	private void extractHeader() {
		StringBuffer sb = new StringBuffer();
		StringTokenizer st = new StringTokenizer(sourceText, "\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			if (line.startsWith("#")) {
				sb.append(line);
				sb.append("\n");
			} else if (line.trim().length() > 0) {
				break;
			}
		}
		sourceHeader = sb.toString();
	}


	public void saveMorphology(TreePoint[] treePoints) {
		StringBuffer sb = new StringBuffer();
		sb.append("# ICING-PSICS " + TimeUtil.timeDateStamp() + "\n");
		if (sourceHeader != null) {
		StringTokenizer st = new StringTokenizer(sourceHeader, "\n");
		while (st.hasMoreTokens()) {
			String line = st.nextToken();
			if (line.indexOf("ICING-PSICS") >= 0) {
				// skip it;
			} else {
				sb.append(line);
				sb.append("\n");
			}
		}
		}

		for (int i = 0; i < treePoints.length; i++) {
			treePoints[i].iwork = i;
		}

		for (TreePoint tp : treePoints) {
			int ipar = -1;
			if (tp.parent != null) {
				ipar = tp.parent.iwork;
			}
			Ball b = tp.getBall();
			String lbl = "0";
			if (tp.hasLabels()) {
				 lbl = tp.getFirstLabel();
			}
			lbl = lbl.replaceAll(" ", "_");
			sb.append(String.format("%6d \"%s\" %10.3f %10.3f %10.3f %8.3f %6d %n",
						tp.iwork, lbl, b.getX(), b.getY(), b.getZ(), b.getRadius(), ipar));
		}
		FileUtil.writeStringToFile(sb.toString(), file);
		E.info("written " + file);
	}

}
