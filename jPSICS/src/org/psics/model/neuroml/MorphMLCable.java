package org.psics.model.neuroml;

import org.psics.be.Meta;
import org.psics.be.MetaContainer;
import org.psics.be.MetaItem;


public class MorphMLCable implements MetaContainer {

	public String id;
	public String name;

	public String fractAlongParent;

	Meta meta;



	public void addMetaItem(MetaItem mi) {
		if (meta == null) {
			meta = new Meta();
		}
		meta.add(mi);
	}



	public String getID() {
		return id;
	}

	public String getName() {
		return name;
	}

}
