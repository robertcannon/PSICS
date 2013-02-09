package org.psics.model.neuroml;

import java.util.ArrayList;


public class ChannelMLOhmicCVRelation {

	public String ion;

	public ChannelMLConductance conductance;

	public String getIonName() {
		return ion;
	}


	public ArrayList<ChannelMLGate> getGates() {
		return conductance.getGates();
	}


	public ArrayList<ChannelMLRateAdjustments> getRateAdjustments() {
		return conductance.getRateAdjustments();
	}


	public void setConductingState(String sid) {
		conductance = new ChannelMLConductance();
		conductance.setConductingState(sid);
	}


}
