package org.psics.model.imports.neuron;

import org.psics.be.Meta;
import org.psics.be.MetaContainer;
import org.psics.be.MetaItem;
import org.psics.be.XMLContainer;


public class NRNProperties implements MetaContainer, XMLContainer {

	Meta meta;


	public void addMetaItem(MetaItem mi) {
		if (meta == null) {
			meta = new Meta();
		}
		meta.add(mi);
	}


	public void setXMLContent(String s) {
		 // ignore - dodgy XML in some cases

	}

}
