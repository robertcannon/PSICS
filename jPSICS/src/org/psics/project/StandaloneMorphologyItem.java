package org.psics.project;

import java.io.File;
import java.util.ArrayList;

import org.psics.be.E;
import org.psics.model.imports.neuron.NRNCell;
import org.psics.model.imports.neuron.NRNMorphML;
import org.psics.model.morph.CellMorphology;
import org.psics.model.morph.DummyCellMorphology;
import org.psics.model.morph.Point;
import org.psics.model.neuroml.MorphML;
import org.psics.model.neuroml.MorphMLCell;
import org.psics.model.neuroml.NeuroML;
import org.psics.quickxml.ElementExtractor;
import org.psics.util.FileUtil;
import org.psics.xml.ReflectionInstantiator;
import org.psics.xml.XMLReader;


public class StandaloneMorphologyItem extends StandaloneItem {


	DummyCellMorphology dummy;



	public StandaloneMorphologyItem(String s, File f) {
		super(s, f, null);
	}


	public String getTypeSummary() {
		return "CellMorphology";
	}

	public Object getObject() {

		if (object == null) {
			String sourceText = FileUtil.readStringFromFile(file);
			// long l1 = System.currentTimeMillis();
			String hdr = sourceText.substring(0, 100);
			if (hdr.indexOf("<CellMorphology") >= 0) {
				object = readPSICSMorphology(sourceText);

			} else if (hdr.indexOf("<morphml") >= 0 && hdr.indexOf("morphml.org") < 0) {
				// DEPRECATE - old style neuron xml files withouth morphml schema ref
				object = readNRNMorphmlMorphology(sourceText, FileUtil.getRootName(file));

			} else if (hdr.indexOf("<morphml") >= 0 || hdr.indexOf("<neuroml") >= 0) {
				object = readMorphmlMorphology(sourceText, FileUtil.getRootName(file));

			} else {
				E.error("cant read : " + hdr);
			}

			// E.info("reading morph took " + (System.currentTimeMillis() - l1));

		}
		return object;
	}


	public Object getDummyObject() {
		if (dummy == null) {
			dummy = new DummyCellMorphology();
		}
		return dummy;
	}



	// this could all be done by the normal XML parser, but the code below is much quicker
	private CellMorphology readPSICSMorphology(String txt) {
		CellMorphology ret = new CellMorphology();

		String[] ptatts = {"id", "x", "y", "z", " r", "parent", "partof", "label", "minor"};

		String sid = ElementExtractor.getAttribute("id", txt.substring(0, 100));
		if (sid != null) {
			ret.setID(sid);
		}

		ArrayList<String> pxmla = ElementExtractor.getElementBodiesOfType("Point", txt);

		String[] srca = pxmla.toArray(new String[pxmla.size()]);
		String[][] ata = ElementExtractor.getAttributeSets(ptatts, srca);
		// int natt = ata.length;
		int iatt = 0;
		E.info("total points " + srca.length);
		for (String[] sa : ata) {
			if (iatt  % 500 == 0) {
				E.info("processing " + iatt + " " + sa[0] + " " + sa[1] + " " + sa[2] + " " + sa[3] + " " + sa[4]);
			}
			Point p = new Point(sa[0], dbl(sa[1]), dbl(sa[2]), dbl(sa[3]), dbl(sa[4]), sa[5],
					sa[6], sa[7]);
			if (sa[8] != null && (sa[8].equals("true") || sa[8].equals("1"))) {
				p.minor = true;
			}
 			ret.add(p);
 			iatt += 1;
		}

		return ret;
	}


	private double dbl(String s) {
		return Double.parseDouble(s);
	}



	private CellMorphology readNRNMorphmlMorphology(String txt, String fileID) {

		ReflectionInstantiator refin = new ReflectionInstantiator();
		refin.addSearchPackage("org.psics.model.imports.neuron");
		refin.addSearchPackage("org.psics.model.imports.neuron.lc");
		XMLReader xmlReader = new XMLReader(refin);
		Object wk = null;
		try {
			wk = xmlReader.read(txt);
		} catch (Exception ex) {
			E.error("Cant read (parse exception) - " + ex);
		}

		CellMorphology ret = null;
		if (wk instanceof NRNCell) {
			ret = ((NRNCell)wk).getCellMorphology(fileID);

		} else if (wk instanceof CellMorphology) {
			ret = (CellMorphology)wk;

		} else if (wk instanceof NRNMorphML) {
			ret = (CellMorphology)((NRNMorphML)wk).getFinal();

		} else {
			E.error("got? " + wk + " cant read " + txt.substring(0, 100));
		}

		return ret;
	}



	private CellMorphology readMorphmlMorphology(String txt, String fileID) {

		E.info("reading a morphml morphology from " + fileID);

		ReflectionInstantiator refin = new ReflectionInstantiator();
		refin.addSearchPackage("org.psics.model.neuroml");
		refin.addSearchPackage("org.psics.model.neuroml.lc");
		XMLReader xmlReader = new XMLReader(refin);
		Object wk = null;
		try {
			wk = xmlReader.read(txt);
		} catch (Exception ex) {
			E.error("Cant read (parse exception) - " + ex);
		}


		CellMorphology ret = null;
		try {
		if (wk instanceof MorphMLCell) {
			ret = ((MorphMLCell)wk).getCellMorphology(fileID);

		} else if (wk instanceof CellMorphology) {
			ret = (CellMorphology)wk;

		} else if (wk instanceof NRNMorphML) {
			ret = (CellMorphology)((NRNMorphML)wk).getFinal();

		} else if (wk instanceof MorphML) {
			ret = (CellMorphology)((MorphML)wk).getFinal();

		} else if (wk instanceof NeuroML) {
			ret = (CellMorphology)((NeuroML)wk).getFinal();

		} else {
			E.error("got? " + wk + " cant read " + txt.substring(0, 100));
		}
		} catch (Exception ex) {
			E.error("cant import: " + ex);
			ex.printStackTrace();
		}

		return ret;
	}
}
