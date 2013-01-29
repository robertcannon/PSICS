package org.psics.num.model.synapse;

// TODO this does nothing so far
public class DefaultSynapseSet implements SynapseSet {

	TableSynapse channel;
	int nchan;
	double erev;
	double gbase;

	public DefaultSynapseSet(TableSynapse chan, int n, double e, double g) {
		 channel = chan;
		 nchan = n;
		 erev = e;
		 gbase = g;
	}


	public void instantiateChannels(double v) {

	}


	public void advance(double v) {

	}

	public double getEEff() {
		return erev;
	}

	public double getGEff() {
		return 0.;
	}

	public String numinfo(double v) {
		return "" + (nchan * gbase);
	}

	public int getNChan() {
		return nchan;
	}

}
