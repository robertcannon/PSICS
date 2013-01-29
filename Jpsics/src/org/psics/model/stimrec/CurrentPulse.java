package org.psics.model.stimrec;

import org.psics.num.CalcUnits;
import org.psics.num.CommandProfile;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Current;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;

@ModelType(info = "A square current pulse", standalone = false, tag = "", usedWithin = { CurrentProfile.class })
public class CurrentPulse extends CProfileFeature {

	@Quantity(range = "[0, 1000)", required = false, tag = "duration of the pulse", units = Units.ms)
	public Time duration = new Time();

	@Quantity(range = "(-50, 50)", required = false, tag = "current to maintain during pulse", units = Units.nA)
	public Current to = new Current();


	public void exportTo(CommandProfile cp) {
		 if (repeatAfter != null && repeatAfter.nonzero()) {
			 cp.addRepeatingBox(CalcUnits.getTimeValue(start), CalcUnits.getCurrentValue(to),
					 	CalcUnits.getTimeValue(duration), CalcUnits.getTimeValue(repeatAfter));

		 } else {
			 cp.addBox(CalcUnits.getTimeValue(start), CalcUnits.getCurrentValue(to),
					 CalcUnits.getTimeValue(duration));
		 }
	}

}
