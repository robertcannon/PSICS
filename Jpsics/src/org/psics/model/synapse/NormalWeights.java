package org.psics.model.synapse;

import org.psics.quantity.phys.NDValue;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.units.Units;



public class NormalWeights extends SynapticWeights {

	@Quantity(range = "(0,)", required = true, tag = "Standard deviation of the dimensionless weight factor", 
			units = Units.none)
	public NDValue sd = new NDValue();
	
}
