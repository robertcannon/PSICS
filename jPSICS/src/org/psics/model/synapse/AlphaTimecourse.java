package org.psics.model.synapse;

import org.psics.num.model.synapse.TableSynapse;
import org.psics.quantity.phys.Time;


public class AlphaTimecourse extends DecayingTimecourse {

	 
	// TODO this is a bit lazy - we treat an alpha synapse as two exponentials with time courses 
	// differing buy 1%. Gives a big normalization later, but generally harmless
	// could extend code to do alpha synapses exactly
	public void applyTo(TableSynapse ret) {
		Time tr = tau.makeCopy();
		tr.multiplyBy(0.99);
		ret.setRise(tau);
		
		ret.addDecay(tau, 1.);
	}

}
