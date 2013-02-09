package org.psics.model.stimrec;

import org.psics.num.CalcUnits;
import org.psics.num.CommandProfile;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Current;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;

@ModelType(info = "Current noise signal for use in a current clamp. It uses a simple" +
		"autoregressive (AR1) model where the timeScale attribute sets the average time for " +
		"staying on the same side of the mean. ",
		standalone = false, tag = "", usedWithin = { Access.class, CurrentClamp.class })
public class CurrentNoise extends CProfileFeature {

	@Quantity(range = "", required = false, tag = "", units = Units.nA)
	public Current mean;

	@Quantity(range = "[0, 50)", required = true, tag = "Overall scale (standard deviation)", units = Units.nA)
	public Current amplitude;

	@Quantity(range = "[0., 100)", required = true, tag = "Smoothing timescale", units = Units.ms)
	public Time timeScale;

	@IntegerNumber(range = "", required = false, tag = "optional seed - generates same sequence each time")
	public NDNumber seed;


	public void exportTo(CommandProfile cp) {
		double zm = CalcUnits.getCurrentValue(mean);
		double za = CalcUnits.getCurrentValue(amplitude);
		double zts = CalcUnits.getTimeValue(timeScale);
		int sd = CalcUnits.getInt(seed);
		cp.addNoise(zm, za, zts, sd);
	}


}
