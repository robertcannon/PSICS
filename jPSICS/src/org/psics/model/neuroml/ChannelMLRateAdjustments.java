package org.psics.model.neuroml;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.model.channel.KSChannel;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.units.Units;


public class ChannelMLRateAdjustments implements AddableTo {

	public ChannelMLParameter offset;
	public ArrayList<ChannelMLQ10> q10s = new ArrayList<ChannelMLQ10>();



	public void add(Object obj) {
		if (obj instanceof ChannelMLQ10) {
			q10s.add((ChannelMLQ10)obj);
		} else {
			E.typeError(obj);
		}

	}



	public void applyTo(KSChannel ksch) {
		 for (ChannelMLQ10 cmq : q10s) {
			 String sg = cmq.getGate();
			 double f = cmq.getFactor();
			 double t = cmq.getExperimentalTemperature();

			 Temperature temp = new Temperature(t, Units.Celsius);

			 if (sg != null) {
				 ksch.setComplexQ10(sg, f, temp);
			 } else {
				 ksch.setGlobalQ10(f, temp);
			 }
		 }
	}

}
