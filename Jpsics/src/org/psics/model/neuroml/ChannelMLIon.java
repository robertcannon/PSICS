package org.psics.model.neuroml;

import org.psics.be.E;


public class ChannelMLIon {

	public String name;
	public String charge;
	public String default_erev;





	public String getSymbol() {
		return getSymbol(name);
	}




	public static String getSymbol(String snm) {
		String ret = null;

		String lcn = snm.toLowerCase();
		if (lcn.equals("na")) {
			ret = "Na";

		} else if (lcn.equals("k")) {
			ret = "K";

		} else if (lcn.equals("ca")) {
			ret = "Ca";

		} else if (lcn.equals("cl")) {
			ret = "Cl";
		}


		if (ret == null) {
			E.oneLineWarning("Channel references unknown " + snm);
			ret = snm;
		}
		return ret;
	}


}
