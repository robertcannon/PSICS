package org.psics.num;


// represents channel props at a particular membrane potential (usually rest)
public class ChannelGE {

	public double g;
	public double e;



	public ChannelGE(double xg, double xe) {
		g = xg;
		e = xe;
	}

}
