package org.psics.model.neuroml;



public class ChannelMLTransition {

	public String src;

	public String target;


	public ChannelMLVoltageGate voltage_gate;


	public ChannelMLVoltageGate getVoltageGate() {
		return voltage_gate;
	}


	public String getSource() {
		return src;
	}

	public String getTarget() {
		return target;
	}

}
