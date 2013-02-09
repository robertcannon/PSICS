package org.psics.model.channel;

import org.psics.be.Exampled;
import org.psics.num.model.channel.TransitionType;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Rate;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;


@ModelType(info = "A one-way transition as used in the original HH model, " +
		"giving the rate, beta, as beta = rate exp((v - midpoint)/ vscale)", standalone = false, tag = "Exponential HH style rate", usedWithin = { KSChannel.class })
public class ExpTransition extends KSTransition implements Exampled {

	@Quantity(units=Units.per_ms, range="(0.01, 1000)", tag="Rate at v = midpoint", required=true)
	public Rate rate;

	@Quantity(units=Units.mV, range="(0.0, 100)", tag="Scale factor for voltage term", required=true)
	public Voltage scale;

	@Quantity(units=Units.mV, range = "(-80, -20)", tag="Potential of inflection between " +
			"exponential and linear dependiecies", required=true)
	public Voltage midpoint;



	public void setRate_ms(double d) {
		rate = new Rate(d, Units.per_ms);
	}

	public void setMidpoint_mV(double d) {
		midpoint = new Voltage(d, Units.mV);
	}

	public void setScale_mV(double d) {
		scale = new Voltage(d, Units.mV);
	}


	public TransitionType getTransitionType() {
		return TransitionType.EXP_ONE_WAY;
	}


	public double[] getTransitionData() {
		double[] ret = new double[5];
		writeTempDependence(ret);
		ret[2] = rate.getValue(Units.per_ms);
		ret[3] = midpoint.getValue(Units.mV);
		ret[4] = scale.getValue(Units.mV);
		return ret;
	}


	public String getExampleText() {
		 return "<ExpTransition rate=\"1.2ms\" midpoint=\"-45.0mV\" scale=\"12mV\"/>";
	}


	@Override
	public ExpTransition makeCopy(KSState sa, KSState sb) {
		ExpTransition ret = new ExpTransition();
		ret.setEnds(sa, sb);
		ret.rate = rate.makeCopy();
		ret.midpoint = midpoint.makeCopy();
		ret.scale = scale.makeCopy();
		copyTemperatureTo(ret);
		return ret;
	}


	@Override
	public ExpTransition makeMultiCopy(KSState sa, KSState sb, double ff, double fr) {
		 ExpTransition ret = makeCopy(sa, sb);
		 ret.rate.multiplyBy(ff);
		 return ret;
	}




}
