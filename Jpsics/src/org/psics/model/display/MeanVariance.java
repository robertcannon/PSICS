package org.psics.model.display;

import org.psics.be.AddableTo;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.SubComponent;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;

@ModelType(info = "Mean-variance analysis of data. This includes all the parameters of a " +
		"LineSet, but instead of plotting the raw data, it computes the mean and variance" +
		"over the given time window and plots the variance against the mean ",
		standalone = false, tag = "Plot of variance against mean", usedWithin = { ViewConfig.class })
public class MeanVariance extends BaseLineSet implements AddableTo {

	@Quantity(range = "(0,)", required=false, units = Units.none, tag="Lower bound of region of interest")
	public NDValue tmin = new NDValue(0.);

	@Quantity(range = "(0,)", required=false, units = Units.none, tag="Upper bound of region of interest")
	public NDValue tmax = new NDValue(0.);

	@Quantity(range = "(0,)", required=false, units = Units.none, tag="Bin size for means")
	public NDValue binSize = new NDValue(0.);

	@SubComponent(contentType = Comparison.class, tag = "Optional theoretical values for comparison")
	public Comparison comparison = new Comparison();


	public void add(Object obj) {
		if (obj instanceof Comparison) {
			comparison = (Comparison)obj;
		}
	}


	public boolean hasComparision() {
		boolean ret = false;
		if (comparison != null && getComparisonNChannel() > 0) {
			ret = true;
		}
		return ret;
	}

	public int getComparisonNChannel() {
		return comparison.getNChannel();
	}

	public double getComparisonGSingle() {
		return comparison.getISingle();
	}


	public double[] getRange() {
		double[] ret = new double[2];
		ret[0] = tmin.getValue();
		ret[1] = tmax.getValue();
		return ret;
	}


	public double getBinSize() {
		return binSize.getValue();
	}



}
