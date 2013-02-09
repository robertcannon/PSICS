package org.psics.distrib;


public class DistribExclusion {

	String winner;
	String loser;


	public DistribExclusion(String a, String b) {
		winner = a;
		loser = b;
	}

	public String getWinner() {
		return winner;
	}

	public String getLoser() {
		return loser;
	}


}
