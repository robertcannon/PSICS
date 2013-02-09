package org.psics.model.stimrec;

import org.psics.num.CalcUnits;
import org.psics.num.CommandProfile;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Conductance;
import org.psics.quantity.units.Units;


@ModelType(info = "A simple step from the current holding conductance to a new one", standalone = false,
		tag = "", usedWithin = { ConductanceProfile.class })
public class ConductanceStep extends GProfileFeature {


	@Quantity(range = "[0, 50)", required = false, tag = "new holding conductance", units = Units.nS)
	public Conductance to;


	public void exportTo(CommandProfile cp) {
		 if (repeatAfter != null && repeatAfter.nonzero()) {
			 cp.addRepeatingStep(CalcUnits.getTimeValue(start), CalcUnits.getConductanceValue(to),
					 CalcUnits.getTimeValue(repeatAfter));

		 } else {
			 cp.addStep(CalcUnits.getTimeValue(start), CalcUnits.getConductanceValue(to));
		 }

	}


}
