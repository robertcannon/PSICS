package org.psics.model.neuroml;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.model.neuroml.lc.gate;


public class ChannelMLConductance implements AddableTo {

	public String default_gmax;


	public ArrayList<ChannelMLGate> gates = new ArrayList<ChannelMLGate>();

	public ArrayList<ChannelMLRateAdjustments> ras = new ArrayList<ChannelMLRateAdjustments>();



	public void add(Object obj) {
		if (obj instanceof ChannelMLGate) {
			gates.add((ChannelMLGate)obj);

		} else if (obj instanceof ChannelMLRateAdjustments) {
			ras.add((ChannelMLRateAdjustments)obj);

		} else {
			E.typeError(obj);
		}
	}



	public ArrayList<ChannelMLGate> getGates() {
		 return gates;
	}



	public ArrayList<ChannelMLRateAdjustments> getRateAdjustments() {
		 return ras;
	}



	public void setConductingState(String sid) {
		 gate g = new gate();
		 g.setConductingState(sid);
	}


}
