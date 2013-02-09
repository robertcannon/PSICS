package org.psics.num;

import org.psics.be.E;
import org.psics.util.TextDataWriter;


public class ChannelCurrentRecorder extends ChannelRecorder {


	public ChannelCurrentRecorder(String id, String at, LineStyle style, String ch) {
		super (id, at, style, ch);
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


	public void appendTo(TextDataWriter tdw) {
		tdw.add(id);
		tdw.addInts(compartment.getIndex(), 3, channelIndex);
		tdw.addMeta("target, type, channel index    " +
				"for " + channelType + " current recorder at " + at);
		tdw.endRow();
	}





}
