package org.psics.model.display;

import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;


@ModelType(info = "Defines the axis ranges for a plot",
		standalone = false, tag = "A view on the data", usedWithin = { LineGraph.class })
public class View {


	@Quantity(range = "()", required = false, tag = "lower value of X axis", units = Units.none)
	public NDValue xmin = new NDValue(Double.NaN);

	@Quantity(range = "()", required = false, tag = "upper value of X axis", units = Units.none)
	public NDValue xmax = new NDValue(Double.NaN);

	@Quantity(range = "()", required = false, tag = "lower value of Y axis", units = Units.none)
	public NDValue ymin = new NDValue(Double.NaN);

	@Quantity(range = "()", required = false, tag = "upper value of Y axis", units = Units.none)
	public NDValue ymax = new NDValue(Double.NaN);

	@Identifier(tag = "ID for this view - the ids should be unique within a ViewConfig definition")
	public String id;


	public boolean showStats = false;

	
	public String getID() {
		 return id;
	}

	public double[] getXYXYLimits() {
		double[] ret = {xmin.getNativeValue(), ymin.getNativeValue(),
				xmax.getNativeValue(), ymax.getNativeValue()};
		return ret;
	}

}
