package org.psics.model.stimrec;

import org.psics.num.CalcUnits;
import org.psics.num.CommandProfile;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Current;
import org.psics.quantity.units.Units;


@ModelType(info = "A simple step from the current holding current to a new one", standalone = false,
		tag = "", usedWithin = { CurrentProfile.class })
public class CurrentStep extends CProfileFeature {



	@Quantity(range = "(-10, 10)", required = false, tag = "new holding potential", units = Units.nA)
	public Current to;





	public void exportTo(CommandProfile cp) {
		 if (repeatAfter != null && repeatAfter.nonzero()) {
			 cp.addRepeatingStep(CalcUnits.getTimeValue(start), CalcUnits.getCurrentValue(to),
					 CalcUnits.getTimeValue(repeatAfter));

		 } else {
			 cp.addStep(CalcUnits.getTimeValue(start), CalcUnits.getCurrentValue(to));
		 }

	}


}
