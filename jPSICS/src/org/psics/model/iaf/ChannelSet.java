package org.psics.model.iaf;

import org.psics.model.channel.KSChannel;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.phys.IntegerQuantity;
import org.psics.quantity.annotation.IntegerNumber;

@ModelType(info = "A population of channels for an IaF cell. It just " +
		"requires the channel type and the number of channels.", standalone = false, 
		tag = "Population of channels for an IaFCell", usedWithin = { IaFCell.class })
public class ChannelSet {
	
	@ReferenceByIdentifier(tag="The channel type",
			targetTypes={KSChannel.class}, required=true, location=Location.global)
	public String channel;
	public KSChannel r_channel;

	@IntegerNumber(range = "[0, 10^6)", required = false, tag = "Total number of channels in the population")
	public IntegerQuantity number;
 
	
}
