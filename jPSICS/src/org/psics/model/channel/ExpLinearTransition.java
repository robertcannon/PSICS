package org.psics.model.channel;

import org.psics.be.E;
import org.psics.be.Exampled;
import org.psics.num.model.channel.TransitionType;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Rate;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;


@ModelType(tag="HH style one-way exponential-linera transition",
	info="A one-way transition expressed in the form A x / (1 - exp(-x))  where " +
			"x = (v - v0) / B.  The parameters are the rate, A, the modpoint, v0, where " +
			"x = 0, and the voltage scale, scale, which divides both the midpoint and v0. Note " +
			"that this expression has numerous variants, so published parameters may not map " +
			"directly onto the rate, midpoint and scale.  Most often the signs of  the rate and scale" +
			" are reversed or the scale dependence of the numerator is wrapped into the rate, " +
			"in a form sucha as  alpha (v - v0) / (exp (beta  (v - v0)) - 1).",
			standalone = false, usedWithin = { KSChannel.class, KSComplex.class })
public class ExpLinearTransition extends KSTransition implements Exampled {

	@Quantity(units=Units.per_ms, range="(0.01, 1000)", tag="Rate at inflection", required=true)
	public Rate rate;

	@Quantity(units=Units.mV, range = "(-80, -20)", tag="Potential of inflection between " +
			"exponential and linear dependiecies", required=true)
	public Voltage midpoint;

	@Quantity(units=Units.mV, range="(-80, 40)", tag="Potential scaling", required=true)
	public Voltage scale;




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
		return TransitionType.EXP_LINEAR_ONE_WAY;
	}


	public double[] getTransitionData() {
		double[] ret = new double[5];
		writeTempDependence(ret);
		ret[2] = rate.getValue(Units.per_ms);
		ret[3] = midpoint.getValue(Units.mV);
		ret[4] = scale.getValue(Units.mV);

		if (ret[2] == 0.) {
			E.fatalError("zero scale for voltage dependence " + getFrom() + " " + getTo() + " " + this);
		}
		return ret;
	}


	public String getExampleText() {
		 return "<ExpLinearTransition rate=\"1.2ms\" midpoint=\"-45.0mV\" scale=\"12mV\"/>";

	}


	@Override
	public ExpLinearTransition makeCopy(KSState sa, KSState sb) {
		ExpLinearTransition ret = new ExpLinearTransition();
		ret.setEnds(sa, sb);
		ret.rate = rate.makeCopy();
		ret.midpoint = midpoint.makeCopy();
		ret.scale = scale.makeCopy();
		copyTemperatureTo(ret);
		return ret;
	}


	@Override
	public ExpLinearTransition makeMultiCopy(KSState sa, KSState sb, double ff, double fr) {
		 ExpLinearTransition ret = makeCopy(sa, sb);
		 ret.rate.multiplyBy(ff);
		 return ret;
	}




}
