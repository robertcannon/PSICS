package org.psics.model.display;

import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;


public class Axis {

	@Quantity(range = "()", required = false, tag = "lower limit", units = Units.none)
	public NDValue min = new NDValue(0);

	@Quantity(range = "()", required = false, tag = "upper limit", units = Units.none)
	public NDValue max = new NDValue(1);

	@Label(info = "", tag = "axis label")
	public String label;


	public String getLabel() {
		return label;
	}



}
