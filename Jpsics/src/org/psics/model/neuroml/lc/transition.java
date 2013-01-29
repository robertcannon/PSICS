package org.psics.model.neuroml.lc;

import org.psics.model.channel.KSTransition;
import org.psics.model.neuroml.ChannelMLTransition;
import org.psics.model.neuroml.ChannelMLVoltageGate;


public class transition extends ChannelMLTransition {

	public void populateFrom(KSTransition kst) {
			src = kst.getFromName();
			target = kst.getToName();

			voltage_gate = new ChannelMLVoltageGate();

			voltage_gate.populateFrom(kst);

	}

}
