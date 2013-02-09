package org.psics.num;

import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

public class Discretization {

	Voltage vmin;
	Voltage vmax;
	Voltage deltaV;


	public Discretization(Voltage v0, Voltage v1, Voltage dv) {
		vmin = v0;
		vmax = v1;
		deltaV = dv;
	}



	public Discretization() {
		 vmin = new Voltage(-80., Units.mV);
		 vmax = new Voltage(60., Units.mV);
		 deltaV = new Voltage(0.3, Units.mV);
	}



	public Voltage getVMin() {
		return vmin;
	}

	public Voltage getVMax() {
		return vmax;
	}

	public Voltage getDeltaV() {
		return deltaV;
	}

}
