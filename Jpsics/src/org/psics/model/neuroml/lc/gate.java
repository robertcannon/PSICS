package org.psics.model.neuroml.lc;

import org.psics.model.neuroml.ChannelMLGate;


public class gate extends ChannelMLGate {

	public void setConductingState(String sid) {
		 state = new state();
		 state.name = sid;
		state.fraction = 1.;
	}

}
