package org.psics.model.neuroml;


public class ChannelMLHHGate {

	public String state;

	public ChannelMLTransition transition;

	public ChannelMLVoltageGate getVoltageGate() {
		 return transition.getVoltageGate();
	}

	public String getState() {
		 return state;
	}

	public boolean isVoltage() {
		boolean ret = false;
		if (getVoltageGate() != null) {
			ret = true;
		}
		return ret;
	}

}
