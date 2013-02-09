package org.psics.model.neuroml;

import org.psics.be.E;
import org.psics.be.ImportException;
import org.psics.be.Meta;
import org.psics.be.MetaContainer;
import org.psics.be.MetaItem;
import org.psics.be.Transitional;
import org.psics.model.channel.KSChannel;

public class ChannelML implements MetaContainer, Transitional {

	public String id;

	// TODO read this all into an xmlns hash map;
	public String xmlns;
	public String xmlns_mml;
	public String xmlns_meta;
	public String xmlns_cml;
	public String xmlns_xsi;
	public String xsi_schemaLocation;


	public String name;
	public String units;

	public ChannelMLIon ion;
	public ChannelMLChannelType channel_type;

	Meta meta;




	public void addMetaItem(MetaItem mi) {
		if (meta == null) {
			meta = new Meta();
		}
		meta.add(mi);
	}




	public Object getFinal() throws ImportException {
		KSChannel ret = null;
		if (channel_type == null) {
			E.error("no channel type in channelml file? " + name);
		} else {
			ret = channel_type.makeKSChannel();

		}
		return ret;
	}




	public void setXMLSchemaSources() {
		 xmlns="http://morphml.org/channelml/schema";
	     xmlns_xsi="http://www.w3.org/2001/XMLSchema-instance";
		 xmlns_meta="http://morphml.org/metadata/schema";
	     xsi_schemaLocation="http://morphml.org/channelml/schema  ../../Schemata/v1.3/Level2/ChannelML_v1.3.xsd";
	     units="Physiological Units";
	}


}
