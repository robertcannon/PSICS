package org.psics.num;

import java.util.HashMap;

import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

public class RunControl {

	Time runTime;
	Time timeStep;
	Voltage v0;

	int dfltStochThreshold;

	double weightingFactor = -1;

	int nRuns;

	HashMap<String, Integer> stochThresholds = new HashMap<String, Integer>();

	boolean natve = false;

	boolean oneByOne = false;



	public RunControl(Time rt, Time ts, Voltage v) {
		runTime = rt;
		timeStep = ts;
		v0 = v;
		nRuns = 1;
	}

	public void setStartPotential(Voltage v) {
		v0 = v;
	}

	public void setOneByOne() {
		oneByOne = true;
	}

	public boolean advanceOneByOne() {
		return oneByOne;
	}



	public int getStochThreshold() {
		return dfltStochThreshold;
	}

	public HashMap<String, Integer> getStochThresholds() {
		return stochThresholds;
	}



	public Voltage getStartPotential() {
		if (v0 == null) {
			v0 = new Voltage(-60., Units.mV);
		}
		return v0;
	}

	public Time getTimeStep() {
		 return timeStep;
	}

	public Time getRunTime() {
		return runTime;
	}

	public void setDefaultStochThreshold(int st) {
		dfltStochThreshold = st;

	}

	public void setStochThreshold(String channelID, int threshold) {
		stochThresholds.put(channelID, new Integer(threshold));

	}


	public void setWeightingFactor(double f) {
		weightingFactor = f;
	}

	public double getWeightingFactor() {
		return weightingFactor;
	}

	public void setUseNative(boolean b) {
		 natve = b;
	}

	public boolean useNative() {
		return natve;
	}

	public void setNRuns(int n) {
		nRuns = n;
	}

	public int getNRuns() {
		return nRuns;
	}
}
