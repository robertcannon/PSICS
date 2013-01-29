package org.psics.num;

import org.psics.be.E;


public abstract class ChannelRecorder extends Accessor {

	String channelType;
	int channelIndex = -1;

	public ChannelRecorder(String id, String at, LineStyle style, String ch) {
		super (id, at, style, true);
		channelType = ch;
	}

	public double getValue() {
		double ret = 0.;
		if (compartment != null) {
			ret = compartment.v;
		}
		E.missing();
		return ret;
	}

	public String getChannelType() {
		return channelType;
	}

	public void setChannelIndex(int i) {
		channelIndex = i;
	}






}
