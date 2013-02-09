package org.psics.model.synapse;

import org.psics.num.model.synapse.TableSynapse;


public class ExponentialTimecourse extends DecayingTimecourse {

 
	public void applyTo(TableSynapse ret) {
		ret.addDecay(tau, 1.);
	}

	
	
	
	
}
