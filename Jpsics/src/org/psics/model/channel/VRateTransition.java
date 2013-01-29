package org.psics.model.channel;


import org.psics.num.model.channel.TransitionType;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Charge;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.phys.Rate;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;


@ModelType(standalone=false, usedWithin={KSChannel.class}, tag="Voltaqe dependent transition defined by forward and reverse rates are v=0",
		info="This is one of two ways of expressing a first order Boltzmann transition with one or two extra " +
				"term(s) to make the rates saturate rather than become indefinitely large for extreme potentials. " +
				"This version requires the forward and reverse rates for a membrane potential of zero. Internally, " +
				"these transitions are converted into exactly the same form as is used for the VHalfTransition.")
public class VRateTransition extends KSTransition   {


	@Quantity(units=Units.per_ms, range = "(0.01, 1000)", required=true,
			tag="Forward rate at zero potential difference")
	public Rate forward;

	@Quantity(units=Units.per_ms, range = "(0.01, 1000)", required=true,
			tag="Reverse rate at zero potential difference")
	public Rate reverse;

	@Quantity(units=Units.e, range = "(-5, 5)", required=true,
			tag="Equivalent gating charge")
	public Charge z;

	@Quantity(units=Units.none, range = "[0., 1.]", required=true,
			tag="Gating assymetry")
	public NDValue gamma;

	@Quantity(units=Units.ms, range = "(0.0001, 1)", required=true,
			tag="Saturation time constant")
	public Time tauMin;





	public TransitionType getTransitionType() {
		return TransitionType.BOLTZMANN_VDEP;
	}


	public double[] getTransitionData() {
		double[] ret = new double[7];
		writeTempDependence(ret);
		ret[2] = forward.getValue(Units.ms);
		ret[3] = reverse.getValue(Units.ms);
		ret[4] = z.getValue(Units.e);
		ret[5] = gamma.getValue(Units.e);
		ret[6] = tauMin.getValue(Units.ms);
		return ret;
	}



	public String getExampleText() {
		return "<VRateTransition from=\"C3\" to=\"O\" forward=\"3.2per_ms\"  reverse=\"0.2per_ms\"  " +
		"z=\"2.3e\"  gamma=\"0.8\"  tauMin=\"0.02ms\"/>";
	}


	@Override
	public VRateTransition makeCopy(KSState sa, KSState sb) {
		 VRateTransition vrt = new VRateTransition();
		 vrt.setEnds(sa, sb);
		 vrt.forward = forward.makeCopy();
		 vrt.reverse = reverse.makeCopy();
		 vrt.z = z.makeCopy();
		 vrt.tauMin = tauMin.makeCopy();
		 vrt.gamma = gamma.makeCopy();
		 copyTemperatureTo(vrt);
		 return vrt;
	}


	@Override
	public VRateTransition makeMultiCopy(KSState sa, KSState sb, double ff, double fr) {
		VRateTransition ret = makeCopy(sa, sb);
		ret.forward.multiplyBy(ff);
		ret.reverse.multiplyBy(fr);
		return ret;
	}

}
