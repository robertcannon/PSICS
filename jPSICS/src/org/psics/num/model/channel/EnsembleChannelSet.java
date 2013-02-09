package org.psics.num.model.channel;
 

public final class EnsembleChannelSet implements ChannelSet {

	TableChannel table;

	double[][] fstate;
	int nchan;

	double eeff;
	double geff;


	static int nrep = 0;



	public EnsembleChannelSet(TableChannel tch, int n) {
		 table = tch;
		 // fstate = tch.equlibriumOccupancy(v0);
		 nchan = n;

		 /*
		 if (nrep < 3) {
			 nrep += 1;
			 E.dump(fstate);
			 E.info("fstate at 0mV");
			 E.dump(tch.equlibriumOccupancy(0.));
		 }
		 */
	}


	public void instantiateChannels(double v0) {
		 fstate = table.equlibriumOccupancy(v0);
	}


	public void advance(double v) {
		table.ensembleAdvance(v, fstate);
		eeff = table.erev; // just ohmic for now;
		// E.info("ens nch open "  + (nchan * table.ensembleGeff(fstate)));
		geff = table.gBase * nchan * table.ensembleGeff(fstate);
	}



	public String numinfo(double v) {
		return "" + fstate[0][0] + " " + fstate[0][1] + " " + table.numinfo(v);
	}


	public double getEEff() {
		return eeff;
	}


	public double getGEff() {
		return geff;
	}


	public int getNChan() {
		return nchan;
	}



}
