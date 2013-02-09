package org.psics.model.synapse;

import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;


public abstract class SynapticWeights {

	@Quantity(range = "[0,1]", required = false, 
			tag = "Mininimum weight (dimensionless) as a fracton of the baseCondctance of the synapse", units = Units.none)
	public NDValue min = new NDValue();
	
	@Quantity(range = "[1,)", required = false, tag = "Maximum weight (dimensionless) as a multiple of the " +
			"baseConductance of the synapse", units = Units.none)
	public NDValue max = new NDValue();
	
	
	
}
