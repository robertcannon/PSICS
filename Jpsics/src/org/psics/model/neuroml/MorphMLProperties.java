package org.psics.model.neuroml;

import org.psics.be.Meta;
import org.psics.be.MetaContainer;
import org.psics.be.MetaItem;
import org.psics.be.XMLContainer;


public class MorphMLProperties implements XMLContainer, MetaContainer {

	Meta meta;

	public void setXMLContent(String s) {
		// we just ignore this - in Neuron 5.9 export it contains invalid XML which
		// would otherwise stop the parser
	}


	public void addMetaItem(MetaItem mi) {
		if (meta == null) {
			meta = new Meta();
		}
		meta.add(mi);
	}

}
