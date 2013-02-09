package org.psics.model.morph;import java.util.StringTokenizer;

import org.psics.be.E;
import org.psics.be.TextFileSourced;
import org.psics.model.control.PSICSRun;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.ReferenceToFile;
import org.psics.quantity.annotation.StringEnum;

@ModelType(info = "", standalone = false, tag = "defines an external source for morphology data",
		usedWithin = { PSICSRun.class })
public class MorphologySource implements TextFileSourced {

	@Identifier(tag = "optional identifier for use if the feature is to be modified")
	public String id = "";


	@ReferenceToFile(tag = "name of the file with time series data",
			fallback = "", required = true)
	public String file;

	@StringEnum(required = true, tag = "format of the source file", values = "SWC, NRN")
	public String format;


	@StringEnum(required = false, tag = "Section style - whether to use cylindrical or tapered sections", values = "TAPERED, UNIFORM")
	public String sections;



	String sourceText;

	CellMorphology cellMorphology;


	public String getID() {
		return id;
	}


	public String getFileName() {
		return file;
	}


	public void setText(String txt) {
		sourceText = txt;
	}


	private void readMorphology() {
		cellMorphology = new CellMorphology();
		if (sourceText == null || sourceText.trim().length() < 10) {
			E.error ("no source text for morphology " + file);
		}

		if (sections == null) {
			// OK
		} else if (sections.equals("uniform")) {
			cellMorphology.setDefaultUniform();

		} else if (sections.equals("tapered")) {
			cellMorphology.setDefaultTapered();

		} else {
			E.warning("unrecognized section style " + sections);
		}


		StringTokenizer lines = new StringTokenizer(sourceText, "\n");
		int npt = 0;
		while (lines.hasMoreTokens()) {
			String line = lines.nextToken().trim();
			if (line.startsWith("#")) {
				// skip it;
			} else {
				if (line.trim().length() > 0) {
					StringTokenizer st = new StringTokenizer(line, " ");
					if (st.countTokens() != 7) {
						E.warning("swc line found that doesn't have 7 elements? ignoring " + line);
					} else {
						String aid = st.nextToken();
						String atyp = st.nextToken();
						double x = Double.parseDouble(st.nextToken());
						double y = Double.parseDouble(st.nextToken());
						double z = Double.parseDouble(st.nextToken());
						double r = Double.parseDouble(st.nextToken());
						String pid = st.nextToken();

						if (pid.startsWith("-")) {
							pid = null;
						}

						atyp = atyp.replaceAll("\"", "");

						cellMorphology.addPoint(aid, x, y, z, r, atyp, pid);


						if (npt < 20) {
						//	E.info("reading morpy " + x + " " + y + " " + r);
						}

						npt += 1;

					}
				}
			}
		}
		cellMorphology.resolve();
	}





	public CellMorphology getCellMorphology() {
		if (cellMorphology == null) {
			readMorphology();
		}
		return cellMorphology;
	}

}
