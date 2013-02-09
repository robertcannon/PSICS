package org.psics.num;

public class CompartmentSynapsePopulation {


	 
	String channelID;
	String populationID;
	int nchan;



	public CompartmentSynapsePopulation(String popid, String chid, int nch) {
		populationID = popid;
		channelID = chid;
		nchan = nch;
	}


	public void zero() {
		nchan = 0;
	}

	public String getPopulationID() {
		return populationID;
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



	public int getNSynapse() {
		return nchan;
	}

}
