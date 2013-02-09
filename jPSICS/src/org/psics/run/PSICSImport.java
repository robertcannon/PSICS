package org.psics.run;

import java.io.File;
import java.util.ArrayList;

import org.psics.be.E;
import org.psics.model.electrical.CellProperties;
import org.psics.model.electrical.ChannelPopulation;
import org.psics.model.electrical.DistributionRule;
import org.psics.model.morph.CellMorphology;
import org.psics.model.neuroml.MorphMLCell;
import org.psics.model.neuroml.NeuroML;
import org.psics.model.neuroml.NeuroMLProp;
import org.psics.model.neuroml.lc.initialMembPotential;
import org.psics.model.neuroml.lc.specificAxialResistance;
import org.psics.model.neuroml.lc.specificCapacitance;
import org.psics.om.Serializer;
import org.psics.util.FileUtil;
import org.psics.util.JUtil;
import org.psics.xml.ReflectionInstantiator;
import org.psics.xml.XMLReader;

public class PSICSImport {

	File rootFile;
	File outFolder;

	Class<?> rootClass;
	String rootName;




	public PSICSImport(File f) {
		rootName = f.getName();
		if (!f.exists()) {
			E.error("no such file " + f);
		}  else {
			rootFile = f;
			outFolder = FileUtil.extensionSibling(f, "-psics");
		if (!outFolder.exists()) {
			outFolder.mkdir();
		}
		}
	}


	public PSICSImport(Class<?> cls, String rnm) {
		rootClass = cls;
		rootName = rnm;
		rootFile = null;
		outFolder = null;
	}


	public void setDestinationFolder(File f) {
		outFolder = f;
	}

	public void convert() {
		ReflectionInstantiator refin = new ReflectionInstantiator();
		refin.addSearchPackage("org.psics.model.neuroml");
		refin.addSearchPackage("org.psics.model.neuroml.lc");
		XMLReader xmlReader = new XMLReader(refin);

		String srctxt = null;
		if (rootFile != null) {
			srctxt = FileUtil.readStringFromFile(rootFile);
		} else {
			srctxt = JUtil.getRelativeResource(rootClass, rootName);
		}

		srctxt = sanitize(srctxt);

		Object wk = null;
		try {
			wk = xmlReader.read(srctxt);
		} catch (Exception ex) {
			E.error("NeuroML Import: can't parse - " + ex);
		}

		if (wk instanceof NeuroML) {
			NeuroML nml = (NeuroML)wk;
			MorphMLCell mmlcell = nml.getMorphMLCell();

			CellMorphology cml = mmlcell.getCellMorphology();
			String s = Serializer.serialize(cml, "%.2f");
			File fc = new File(outFolder, cml.getID() + ".xml");
			FileUtil.writeStringToFile(s, fc);
			E.info("written " + fc.getAbsolutePath());


			boolean empty = true;
			CellProperties cp = new CellProperties();
			cp.setID("membrane");
			ArrayList<DistributionRule> drs = mmlcell.getDistributionRules();
			E.info("ndr " + drs.size());
			if (drs.size() > 0) {
				empty = false;
				cp.addRules(drs);
			}
			ArrayList<ChannelPopulation> cpal = mmlcell.getChannelPopulations();
			E.info("ncp " + cpal.size());
			if (cpal.size() > 0) {
				empty = false;
				cp.addPops(cpal);
			}

			boolean donesc = false;
			boolean donesar = false;
			ArrayList<NeuroMLProp> props = mmlcell.getProperties();
			for (NeuroMLProp p : props) {
				if (p instanceof specificCapacitance) {
					if (donesc) {
						E.missing();
					} else {
						cp.setSurfaceCapacitance(p.getParameters().get(0).getValue());
						donesc = true;
					}
				} else if (p instanceof specificAxialResistance) {
					if (donesar) {
						E.missing();
					} else {
						cp.setAxialResistance(p.getParameters().get(0).getValue());
						donesar = true;
					}


				} else if (p instanceof initialMembPotential) {
					// just ignore it - shouldn't be here;

				} else {
					E.error("cant handle " + p);
				}
			}


			if (!empty) {
				String scp = Serializer.serialize(cp, "%.3f");
				File fcp = new File(outFolder, cp.getID() + ".xml");
				FileUtil.writeStringToFile(scp, fcp);
				E.info("written " + fcp.getAbsolutePath());
			}

		} else {
			E.error("unrecognized import type " + wk);
		}



	}



	private String sanitize(String src) {
		// get rid of all that crazy namespace stuff
		String ret = src.replace("mml:", "");

		ret = ret.replace("bio:", "");

		return ret;
	}


}
