package org.psics.model.channel;

import org.psics.be.Exampled;
import org.psics.num.model.channel.TransitionType;
import org.psics.num.model.channel.TransitionTypes;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Charge;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;


@ModelType(standalone=false, usedWithin={KSChannel.class}, tag="Voltage dependent transition defined by its midpoint",
		info="This is one of two ways of expressing a first order Boltzmann transition with one or two extra " +
				"term(s) to make the rates saturate rather than become indefinitely large for extreme potentials. " +
				"This form uses the mid-pint potential, vHalf at which forward and reverse rates are equal, and the " +
				"timescale (reciprocol of the rate) at that point. The saturation timescale can be expressed either as " +
				"tauMin, or separately for the forward and reverse rates as tauMinFwd and tauMinRev.")
public class VHalfTransition extends KSTransition implements Exampled {

	@Quantity(units=Units.mV, range = "(-80, -20)", required=true,
			tag="Potential at which the forward and reverse rates are equal")
	public Voltage vHalf;

	@Quantity(units=Units.e, range="(-4, 4)", required=true,
			tag="Equivalent gating charg in electronic charge units")
	public Charge z;

	@Quantity(units=Units.none, range="[0,1]", required=true,
			tag="Gating assymetry: relative position of potential peak in the transit of gating particle")
	public NDValue gamma;

	@Quantity(units=Units.ms, range="(0.001, 1)", required=true,
			tag="transition timescale (reciprocol ofthe rate at vHalf)")
	public Time tau;

	@Quantity(units=Units.ms, range="(1.e-5, 1.)", required=false,
			tag="minimum transition timescale (saturation of rate for extreme potentials) -  must either set tauMin or the forward and reverse timescales separately")
	public Time tauMin;

	@Quantity(units=Units.ms, range="(1.e-5, 1.)", required=false,
			tag="minimum transition timescale for forward transition (if tauMin is not set)")
	public Time tauMinFwd;

	@Quantity(units=Units.ms, range="(1.e-5, 1.)", required=false,
			tag="minimum transition timescale for reverse transition (if tauMin is not set)")
	public Time tauMinRev;


	double fwdFactor = 1.;
	double revFactor = 1.;



	public void applyMultipliers(double fm, double rm) {
		fwdFactor *= fm;
		revFactor *= rm;
	}



	public TransitionType getTransitionType() {
		return TransitionType.BOLTZMANN_VDEP;
	}


	private void checkTaus() {
		if (tauMinFwd == null) {
			 tauMinFwd = tauMin.makeCopy();
		}
		if (tauMinRev == null) {
			 tauMinRev = tauMin.makeCopy();
		}
	}

	public double[] getTransitionData() {
		checkTaus();
		double[] ret = new double[10];
		writeTempDependence(ret);

	    double[] wk = TransitionTypes.ratesOfZVGTTT(z, vHalf, gamma, tau, tauMinFwd, tauMinRev, fwdFactor, revFactor);
		for (int i = 0; i < wk.length; i++) {
			ret[2 + i] = wk[i];
		}

	    return ret;
	}


	public String getExampleText() {
		String ret = "<VHalfTransition from=\"C3\" to=\"O\" vHalf=\"-45mV\" z=\"3.5e\"" +
		" gamma=\"0.8\" tau=\"1.2ms\" tauMin=\"0.02ms\"/>";
		return ret;

	}




	public VHalfTransition makeCopy(KSState sa, KSState sb) {
		checkTaus();
		VHalfTransition ret = new VHalfTransition();
		ret.setEnds(sa, sb);
		ret.vHalf = vHalf.makeCopy();
		ret.z = z.makeCopy();
		ret.gamma = gamma.makeCopy();
		ret.tau = tau.makeCopy();
		ret.tauMinFwd = tauMinFwd.makeCopy();
		ret.tauMinRev = tauMinRev.makeCopy();
		ret.fwdFactor = fwdFactor;
		ret.revFactor = revFactor;
		copyTemperatureTo(ret);
		return ret;
	}



	@Override
	public VHalfTransition makeMultiCopy(KSState sa, KSState sb, double ff, double fr) {
		 VHalfTransition ret = makeCopy(sa, sb);

		 /*
		  * NB - can't do the following: the saturation rates mean that just multiplying the fwd and rev rates
		  * _doesn't_ give a transition that produces exact multiples of original rates
		 double[] d = TransitionTypes.ratesOfZVGTTT(z, vHalf, gamma, tau, tauMinFwd, tauMinRev);
		 double[] v = TransitionTypes.vhEtcOfRRRRZG(d[0], d[1], d[2], d[3], d[4], d[5]);
		  */

		 ret.applyMultipliers(ff, fr);

		 return ret;
	}

}
