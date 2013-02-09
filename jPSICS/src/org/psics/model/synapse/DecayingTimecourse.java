package org.psics.model.synapse;

import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;
 

public abstract class DecayingTimecourse extends SynapticTimecourse {

	@Quantity(range = "(0,100)", required = false, tag = "decay timescale: tau in g = gmax exp(-t/tau)", units = Units.ms)
	public Time tau = new Time(20., Units.ms);
	
	
}
