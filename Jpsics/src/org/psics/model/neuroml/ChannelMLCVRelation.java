package org.psics.model.neuroml;

import java.util.ArrayList;




public class ChannelMLCVRelation {

	public ChannelMLOhmicCVRelation ohmic;


	public String getIonName() {
		return ohmic.getIonName();
	}


	public ArrayList<ChannelMLGate> getGates() {
		return ohmic.getGates();
	}


	public ArrayList<ChannelMLRateAdjustments> getRateAdjustments() {
		return ohmic.getRateAdjustments();
	}

	public void setOhmicEtc(String sid, String ionid) {

		ohmic = new ChannelMLOhmicCVRelation();
		ohmic.ion = ionid;
		ohmic.setConductingState(sid);
	}



}
