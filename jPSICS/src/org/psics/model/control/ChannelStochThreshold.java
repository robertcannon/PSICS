package org.psics.model.control;

import org.psics.model.channel.KSChannel;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.IntegerNumber;

@ModelType(standalone = false, usedWithin = { PSICSRun.class }, tag = "Per-channel stochasticity threshold", info = "This provides finer-grained control of the "
		+ "stochastic calculation than the default stochThreshold specification. Any thresholds provided "
		+ "this way apply to the specified channel type across the entire structure.")
public class ChannelStochThreshold {


	@ReferenceByIdentifier(tag = "The channel type", targetTypes = { KSChannel.class }, required = true, location = Location.global)
	public String channel;
	public KSChannel r_channel;

	@IntegerNumber(range = "(10, 1000)", required = false, tag = "The threshold beyond which the calculation"
			+ "for this channel type uses the ensemble limit")
	public int threshold;


	public String getChannelID() {
		String ret = null;
		if (r_channel != null) {
			ret = r_channel.getID();
		}
		return ret;
	}


	public int getThreshold() {
		return threshold;
	}

}
