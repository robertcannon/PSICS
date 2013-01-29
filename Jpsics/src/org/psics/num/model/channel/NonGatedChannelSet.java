package org.psics.num.model.channel;


public class NonGatedChannelSet implements ChannelSet {

	TableChannel channel;
	int nchan;
	double erev;
	double gbase;

	public NonGatedChannelSet(TableChannel chan, int n, double e, double g) {
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
		return nchan * gbase;
	}

	public String numinfo(double v) {
		return "" + (nchan * gbase);
	}

	public int getNChan() {
		return nchan;
	}

}
