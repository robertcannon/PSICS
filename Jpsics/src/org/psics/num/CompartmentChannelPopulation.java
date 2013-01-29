package org.psics.num;

public class CompartmentChannelPopulation {


	String channelID;
	int nchan;



	public CompartmentChannelPopulation(String chid, int nch) {
		channelID = chid;
		nchan = nch;
	}


	public void zero() {
		nchan = 0;
	}

	public String getChannelID() {
		return channelID;
	}

	public void add(int nch) {
		nchan += nch;
	}


	public void setNChan(int n) {
		nchan = n;
	}



	public int getNChan() {
		return nchan;
	}

}
