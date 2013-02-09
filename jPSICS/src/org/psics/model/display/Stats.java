package org.psics.model.display;

import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;


@ModelType(info = "Calculate simple statistics of result data",
		standalone = false,
		tag = "Simple statistics", usedWithin = { LineGraph.class })
public class Stats {


	@Quantity(range = "", required = false, tag = "", units = Units.ms)
	public Time xmin = null;

	@Quantity(range = "", required = false, tag = "", units = Units.ms)
	public Time xmax = null;



	public double getXMin() {
		double ret = Double.NaN;
		if (xmin != null) {
			ret = CalcUnits.getTimeValue(xmin);
		}
		return ret;
	}

	public double getXMax() {
		double ret = Double.NaN;
		if (xmax != null) {
			ret = CalcUnits.getTimeValue(xmax);
		}
		return ret;
	}


}

