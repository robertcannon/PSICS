package org.psics.distrib;

import java.util.HashMap;

import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.num.ChannelGE;
import org.psics.num.Compartment;
import org.psics.num.CompartmentTree;
import org.psics.num.math.MersenneTwister;
import org.psics.quantity.phys.Voltage;


public class ChannelBalance {

	Voltage potential;
	String[] variableChannels;

	public ChannelBalance(Voltage v, String[] vc) {
			potential = v;
			variableChannels = vc;
	}

	public Voltage getPotential() {
		return potential;
	}


	public void applyTo(CompartmentTree ctree, HashMap<String, ChannelGE> geHM, MersenneTwister mersenne) {
		/*
		for (String s : geHM.keySet()) {
			E.info("ge at tareget potential for " + s + " " + geHM.get(s).g + " " + geHM.get(s).e);
		}
		*/


		double vtgt = CalcUnits.getVoltageValue(potential);
		if (variableChannels.length == 1) {
			singleBalance(ctree, vtgt, variableChannels[0], geHM, mersenne);

		} else if (variableChannels.length == 2) {
			doubleBalance(ctree, vtgt, variableChannels[0], variableChannels[1], geHM);

		} else {
			E.missing();
		}
	}



	private void doubleBalance(CompartmentTree ctree, double vtgt, String ch1, String ch2,
				HashMap<String, ChannelGE> geHM) {
		
		int nbal = 0;
		for (Compartment cpt : ctree.getCompartments()) {
			if (cpt.calculateBalanceNumbers(vtgt, ch1, ch2, geHM)) {
				nbal += 1;
			}
		}
		E.info("got " + nbal + " cpts with " + ch1 + " and " + ch2);
		// at this stage we know the fractional number (+ or -) of channels needed on each cpt
		// compartments that don't have both channel types will have zero for both work variables
		ctree.toWork(0);
		ctree.SmoothPositiveIntegerWork();
		ctree.addChannelsFromWork(ch1);


		ctree.toWork(1);
		ctree.SmoothPositiveIntegerWork();
		ctree.addChannelsFromWork(ch2);
		
		E.log("Density adjusted for " + nbal + " compartments with " + ch1 + " and " + ch2);
	}


	@SuppressWarnings("unused")
	private void singleBalance(CompartmentTree ctree, double vtgt, String ch,
			HashMap<String, ChannelGE> geHM, MersenneTwister mersenne) {
		 E.missing();

	}



}
