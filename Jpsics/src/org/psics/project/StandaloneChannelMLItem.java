package org.psics.project;

import java.io.File;

import org.psics.be.E;
import org.psics.be.ImportException;
import org.psics.be.Transitional;
import org.psics.model.channel.DummyKSChannel;
import org.psics.model.channel.KSChannel;
import org.psics.om.Serializer;
import org.psics.util.FileUtil;
import org.psics.xml.ReflectionInstantiator;
import org.psics.xml.XMLReader;


public class StandaloneChannelMLItem extends StandaloneItem {


	Object dummy;


	public StandaloneChannelMLItem(String s, File f) {
		super(s, f, null);
	}


	public String getTypeSummary() {
		return "KSChannel";
	}

	public Object getObject() {

		if (object == null) {
			String sourceText = FileUtil.readStringFromFile(file);
			// long l1 = System.currentTimeMillis();
		 

			KSChannel ksch = readChannelMLChannel(sourceText, FileUtil.getRootName(file));

			if (ksch != null) {

				E.info("ksch " + ksch + " " + ksch.c_states.size() + " " + ksch.c_complexes.size() + " " +
						ksch.c_transitions.size());

				File fcache = FileUtil.getSiblingFile(file, ".psix");
				String sser = Serializer.serialize(ksch);
				FileUtil.writeStringToFile(sser, fcache);

				E.info("written psics equivalent channel in " + fcache);
			}
			object = ksch;


			// E.info("reading morph took " + (System.currentTimeMillis() - l1));

		}
		return object;
	}


	public Object getDummyObject() {
		if (dummy == null) {
			dummy = new DummyKSChannel();
		}
		return dummy;
	}


 


	private KSChannel readChannelMLChannel(String txt, String fileID) {

		E.info("reading a channelML channel type from " + fileID);

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

		if (wk instanceof Transitional) {
			try {
			wk = ((Transitional)wk).getFinal();
			} catch (ImportException ex) {
				E.error("cant read " + fileID + " " + ex);
				wk = null;
			}
		}

		KSChannel ret = null;
		if (wk instanceof KSChannel) {
			ret = (KSChannel)wk;

			//ret = ((MorphMLCell)wk).getCellMorphology(fileID);


		} else if (wk != null) {
			E.oneLineError("need to extract channel spec from " + wk.getClass().getName() + " " + wk);
		}

		return ret;
	}
}
