package org.psics.model.electrical;


import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.model.channel.BaseChannel;
import org.psics.model.channel.DerivedKSChannel;
import org.psics.model.channel.KSChannel;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.ReferenceByIdentifier;


@ModelType(standalone=false, usedWithin={CellProperties.class},
		tag="A population of channels combining a channel type, the base density and either " +
				"a reference to a labelled region of the cell or a distribution rule", info = "")
public class ChannelPopulation extends Population {

	@ReferenceByIdentifier(tag="The channel type",
			targetTypes={KSChannel.class, DerivedKSChannel.class}, required=true, location=Location.global)
	public String channel;
	public BaseChannel r_channel;

	



	public String getChannelID() {
		return channel;
	}

	public KSChannel getKSChannel() {
		KSChannel ret = null;
		if (r_channel instanceof KSChannel) {
			ret = (KSChannel)r_channel;
		} else if (r_channel instanceof DerivedKSChannel) {
			ret = ((DerivedKSChannel)r_channel).getNaturalKCShannel();
		} else {
			E.error("unrecognized channel type " + r_channel);
		}

		return ret;
	}

	@Override
	public String getTargetID() {
		 return channel;
	}

	@Override
	public void setTargetID(String sid) {
		 channel = sid;
	}

	@Override
	public boolean subAdd(Object obj) {
		return false;
	}

}
