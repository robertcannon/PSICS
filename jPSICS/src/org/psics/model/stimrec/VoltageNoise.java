package org.psics.model.stimrec;

import org.psics.num.CalcUnits;
import org.psics.num.CommandProfile;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

@ModelType(info = "Noise signal for use in a voltage clamp. It uses a simple" +
		"autoregressive (AR1) model where the timeScale attribute sets the average time for " +
		"staying on the same side of the mean. ",
		standalone = false, tag = "", usedWithin = { Access.class, VoltageClamp.class })
public class VoltageNoise extends VProfileFeature {

	@Quantity(range = "", required = false, tag = "", units = Units.mV)
	public Voltage mean;

	@Quantity(range = "[0, 50)", required = true, tag = "Overall scale (standard deviation)", units = Units.mV)
	public Voltage amplitude;

	@Quantity(range = "[0., 100)", required = true, tag = "Smoothing timescale", units = Units.ms)
	public Time timeScale;

	@IntegerNumber(range = "", required = false, tag = "optional seed - if set, it generates same sequence each time")
	public NDNumber seed;


	public void exportTo(CommandProfile cp) {
		double zm = CalcUnits.getVoltageValue(mean);
		double za = CalcUnits.getVoltageValue(amplitude);
		double zts = CalcUnits.getTimeValue(timeScale);
		int sn = CalcUnits.getInt(seed);
		cp.addNoise(zm, za, zts, sn);
	}


}
