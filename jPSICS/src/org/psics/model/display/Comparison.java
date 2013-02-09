package org.psics.model.display;

import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;

@ModelType(info = "Comparison model for mean variance analysis. Based on the supplied " +
		"single channel conductance and number of channels it generates the expected variance at " +
		"for a range of values of the mean. Only applicable within a MeanVariance plot", standalone = false,
		tag = "Theoretical curve for a MeanVariance plot", usedWithin = { MeanVariance.class })
public class Comparison {

	@IntegerNumber(range = "(0,)", required=false, tag="number of channels")
	public NDNumber nChannel = new NDNumber(0);

	@Quantity(range = "(0,)", required=false, units = Units.none, tag="single channel conductance")
	public NDValue iSingle = new NDValue(0.);


	public int getNChannel() {
		return nChannel.getNativeValue();
	}


	public double getISingle() {
		return iSingle.getNativeValue();
	}



}

