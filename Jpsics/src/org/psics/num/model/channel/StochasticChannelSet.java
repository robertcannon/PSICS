package org.psics.num.model.channel;

import org.psics.num.math.Random;

public class StochasticChannelSet implements ChannelSet {


	TableChannel table;

	int[][] state;
	int nchan;

	double eeff;
	double geff;


	public StochasticChannelSet(TableChannel tch, int n) {
		 table = tch;
		 nchan = n;
	}




	public void instantiateChannels(double v0) {
		 double[][] fstate = table.equlibriumOccupancy(v0);

		 state = new int[nchan][fstate.length];


	      for (int igc = 0; igc < fstate.length; igc++) {
	    	 double[] fcomp = fstate[igc];
	         for (int i = 0; i < nchan; i++) {
	            state[i][igc] = Random.weightedSample(fcomp);

	         }
	      }
	}



	public void advance(double v) {
		table.stochasticAdvance(v, state);
		eeff = table.erev; // just ohmic for now;
		geff = 0.;
		for (int i = 0; i < nchan; i++) {
			geff += table.stochasticGeff(state[i]);
		}
		geff *= table.gBase;
	}


	public double getEEff() {
		return eeff;
	}


	public double getGEff() {
		return geff;
	}



	public String numinfo(double v) {
		// TODO Auto-generated method stub
		return null;
	}



	public int getNChan() {
		return nchan;
	}




	public TableChannel getTable() {
		 return table;
	}

}
