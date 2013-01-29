package org.psics.model.neuroml;

import org.psics.be.Meta;
import org.psics.be.MetaContainer;
import org.psics.be.MetaItem;


public class ChannelMLStatus implements MetaContainer {


	
	
	public String value;

	Meta meta;




	public void addMetaItem(MetaItem mi) {
		if (meta == null) {
			meta = new Meta();
		}
		meta.add(mi);
	}

}
