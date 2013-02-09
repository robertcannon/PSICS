package org.psics.model.stimrec;

import org.psics.num.CalcUnits;
import org.psics.num.CommandProfile;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;

import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;


@ModelType(info = "A simple step from te current holding potential to a new one", standalone = false,
		tag = "", usedWithin = { VoltageProfile.class })
public class VoltageStep extends VProfileFeature {



	@Quantity(range = "(-100, 50)", required = false, tag = "new holding potential", units = Units.mV)
	public Voltage to;


	public void exportTo(CommandProfile cp) {
		 if (repeatAfter != null && repeatAfter.nonzero()) {
			 cp.addRepeatingStep(CalcUnits.getTimeValue(start), CalcUnits.getVoltageValue(to),
					 CalcUnits.getTimeValue(repeatAfter));

		 } else {
			 cp.addStep(CalcUnits.getTimeValue(start), CalcUnits.getVoltageValue(to));
		 }

	}


}
