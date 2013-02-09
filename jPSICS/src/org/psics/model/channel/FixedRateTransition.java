package org.psics.model.channel;


import org.psics.num.model.channel.TransitionType;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Rate;
import org.psics.quantity.units.Units;



@ModelType(standalone=false, usedWithin={KSChannel.class},
		tag="A transition between states of a kinetic scheme with fixed forward and reverse rates",
		info="A fixed-rate transition, sometimes known as a time-dependent transition, is one where the " +
			 "probability of the transition occuring in a given time interal is always the same, " +
			 "independent of external factors such as the membrane potential.")
public class FixedRateTransition extends KSTransition   {

	@Quantity(units=Units.per_ms, range = "(0.01, 1000)", tag="Forward rate, transitions per ms", required=true)
	public Rate forward;

	@Quantity(units=Units.per_ms, range = "(0.01, 1000)", tag="Reverse rate, transitions per ms", required=true)
	public Rate reverse;


	public TransitionType getTransitionType() {
		return TransitionType.FIXED_RATE;
	}


	public double[] getTransitionData() {
		 double[] ret = new double[4];
		 writeTempDependence(ret);
		 ret[2] = forward.getValue(Units.per_ms);
		 ret[3] = reverse.getValue(Units.per_ms);
		 return ret;
	}


	public String getExampleText() {
		return "<FixedRateTransition forward=\"1.2per_ms\" reverse=\"0.3per_ms\"/>";
	}


	@Override
	public FixedRateTransition makeCopy(KSState sa, KSState sb) {
		FixedRateTransition ret = new FixedRateTransition();
		ret.setEnds(sa, sb);
		ret.forward = forward.makeCopy();
		ret.reverse = reverse.makeCopy();
		copyTemperatureTo(ret);
		return ret;
	}



	@Override
	public FixedRateTransition makeMultiCopy(KSState sa, KSState sb, double ff, double fr) {
		 FixedRateTransition ret = makeCopy(sa, sb);
		 ret.forward.multiplyBy(ff);
		 ret.reverse.multiplyBy(fr);
		 return ret;
	}



}
