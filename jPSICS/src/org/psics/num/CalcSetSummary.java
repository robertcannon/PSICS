package org.psics.num;

import java.util.ArrayList;


public class CalcSetSummary {

	public ArrayList<CalcSummary> sums = new ArrayList<CalcSummary>();



	public CalcSetSummary() {

	}

	public void add(CalcSummary cs) {
		sums.add(cs);
	}

	public ArrayList<CalcSummary> getCalcSummaries() {
		return sums;
	}

	public int nSummaries() {
		return sums.size();
	}

	public CalcSummary getOne() {
		return sums.get(0);
	}



}
