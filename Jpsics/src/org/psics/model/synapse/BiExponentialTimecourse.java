package org.psics.model.synapse;

import org.psics.num.model.synapse.TableSynapse;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;


public class BiExponentialTimecourse extends DecayingTimecourse {

	@Quantity(range = "(1,100)", required = false, tag = "rise time - by analogy with the decay timescale, " +
			"this is the time scale on which the conductance would tend to its maximum value in the absence of " +
			"a decay term ie, tau in g = gmax(1 - exp(-t/tau))", units = Units.ms)
	public Time rise = new Time(2., Units.ms);

	 
	public void applyTo(TableSynapse ret) {
		ret.addDecay(tau, 1.);
		ret.setRise(rise);
	}
	
	
	
	
}
