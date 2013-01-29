package org.psics.model.synapse;

import org.psics.num.model.synapse.TableSynapse;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;
 

public abstract class SynapticTimecourse {

	public abstract void applyTo(TableSynapse ret);
	 
	
}
