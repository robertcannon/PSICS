package org.psics.doc.gen;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.psics.be.E;
import org.psics.be.Element;
import org.psics.model.activity.ActivityComponents;
import org.psics.model.channel.ChannelComponents;
import org.psics.model.control.ControlComponents;
import org.psics.model.display.VisComponents;
import org.psics.model.electrical.ChannelDistributionComponents;
import org.psics.model.environment.EnvironmentComponents;
import org.psics.model.iaf.IaFCellComponents;
import org.psics.model.morph.MorphologyComponents;
import org.psics.model.stimrec.StimComponents;
import org.psics.model.synapse.SynapseComponents;
import org.psics.om.OmBuilder;
import org.psics.util.FileUtil;


public class DocWriter {

	HashMap<String, String> eltHM = new HashMap<String, String>();
	HashMap<String, ArrayList<String[]>> attHM = new HashMap<String, ArrayList<String[]>>();

	static String TYPE_SPEC_TRANSFORM_PATH = "TypeSpec.xsl";

	File destdir = null;

	public DocWriter() {

	}


	public static void main(String[] argv) {
		DocWriter dw = new DocWriter();
		dw.doMain(argv);
	}


	private void doMain(String[] argv) {
		if (argv.length > 0) {
			destdir = new File(argv[0]);
		} else {
			destdir = new File("../tmp");
			E.info("writing xml to " + destdir.getAbsolutePath());
		}

		{
			File fout = new File(destdir, "ChannelComponents.xml");
			writeClassSet(ChannelComponents.channelClasses, fout, "chan");
		}

		{
			File fout = new File(destdir, "ActivityComponents.xml");
			writeClassSet(ActivityComponents.activityClasses, fout, "syn");
		}
		
		{
			File fout = new File(destdir, "SynapseComponents.xml");
			writeClassSet(SynapseComponents.synapseClasses, fout, "syn");
		}
		
		
		{
			File fout = new File(destdir, "ChannelDistributionComponents.xml");
			writeClassSet(ChannelDistributionComponents.channelDistributionClasses, fout, "dist");
		}
		{
			File fout = new File(destdir, "EnvironmentComponents.xml");
			writeClassSet(EnvironmentComponents.environmentClasses, fout, "envi");
		}
		{
			File fout = new File(destdir, "IaFComponents.xml");
			writeClassSet(IaFCellComponents.iafClasses, fout, "iaf");
		}
		{
			File fout = new File(destdir, "ControlComponents.xml");
			writeClassSet(ControlComponents.controlClasses, fout, "cont");
		}

		{
			File fout = new File(destdir, "MorphologyComponents.xml");
			writeClassSet(MorphologyComponents.morphologyClasses, fout, "morp");
		}

		{
			File fout = new File(destdir, "StimComponents.xml");
			writeClassSet(StimComponents.stimClasses, fout, "stim");
		}

		{
			File fout = new File(destdir, "VisComponents.xml");
			writeClassSet(VisComponents.visClasses, fout, "visc");
		}

		{
		File fel = new File(destdir, "Elements.xml");
		OmBuilder omb = new OmBuilder();
		Element elt = omb.newElement("ElementsIndex");
		ArrayList<String> elts = new ArrayList<String>();
		elts.addAll(eltHM.keySet());
		Collections.sort(elts);
		for (String s : elts) {

			Element esub = omb.newElement("IndexEltItem");
			omb.addElement(elt, esub);
			omb.addAttribute(esub, "entry", s);
			omb.addAttribute(esub, "page", eltHM.get(s));
		}
		String selt = elt.serialize();
		FileUtil.writeStringToFile(selt, fel);
		}


		{
			File fal = new File(destdir, "Attributes.xml");
			OmBuilder omb = new OmBuilder();
			Element elt = omb.newElement("AttributesIndex");
			ArrayList<String> atts = new ArrayList<String>();
			atts.addAll(attHM.keySet());
			Collections.sort(atts);
			for (String s : atts) {
				Element esub = omb.newElement("IndexAttItem");
				omb.addElement(elt, esub);
				omb.addAttribute(esub, "entry", s);
				for (String[] sa : attHM.get(s)) {
					Element ess = omb.newElement("IndexRef");
					omb.addElement(esub, ess);
				omb.addAttribute(ess, "page", sa[0]);
				omb.addAttribute(ess, "block", sa[1]);
				}
			}
			String selt = elt.serialize();
			FileUtil.writeStringToFile(selt, fal);
			E.info("written " + fal);
			}


	}




	public void writeClassSet(Class<?>[] ca, File fout, String destroot) {
		OmBuilder omb = new OmBuilder();
		Element elt = omb.newElement("TypeSet");
		for (Class<?> c : ca) {
		 	TypeDocWriter tdw = new TypeDocWriter(c);
		 	Element felt = tdw.getDocElement();
		 	// E.info("one elt " + felt.serialize());
		 	omb.addElement(elt, felt);

		 	String enm = tdw.getElementName();
		 	eltHM.put(enm, destroot);
		 	for (String s : tdw.getAttributes()) {
		 		String[] sa = {destroot, enm};
		 		if (attHM.containsKey(s)) {
		 			attHM.get(s).add(sa);
		 		} else {
		 			ArrayList<String[]> als = new ArrayList<String[]>();
		 			als.add(sa);
		 			attHM.put(s, als);
		 		}
		 	}

		}
		String selt = elt.serialize();
		FileUtil.writeStringToFile(selt, fout);
	}




	/*
	// how to run an xsl transform from java
	private String getTypeSpecificationsHTML(Class[] ca) {

		StringBuffer sb = new StringBuffer();

		for (Class c : ca) {
			 	TypeDocWriter tdw = new TypeDocWriter(c);

			 	Element elt = tdw.getDocElement();
			 	String selt = elt.serialize();

			 	XSLTransformer trans = new XSLTransformer();
			 	String res = trans.transform(selt, getClass().getResourceAsStream(TYPE_SPEC_TRANSFORM_PATH));
			 	sb.append(res);
		 }
		return sb.toString();
	}
	*/



}
