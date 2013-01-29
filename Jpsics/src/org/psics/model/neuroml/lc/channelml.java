package org.psics.model.neuroml.lc;

import org.psics.model.channel.KSChannel;
import org.psics.model.neuroml.ChannelML;


public class channelml extends ChannelML {






	public void populateFrom(KSChannel ksc) {
		setXMLSchemaSources();

		ion = new ion();
		ion.name = ksc.getPermeantIon().getID();

		channel_type = new channel_type();
		channel_type.populateFrom(ksc);
		
	}

}
