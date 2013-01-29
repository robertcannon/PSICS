package org.psics.model.electrical;

import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;

@ModelType(info = "Exclusions impose conditions on the labelling of points after rediscretization. By default," +
		"a compartment in the discretization inherits all the labels from the original points that lie inside it." +
		"Some new ompartments may overlap two distinct property sets so if propertiesare attached according" +
		"to the labels, some compartments may get two sets of prpoerties. To avoid this, an exclusion rule" +
		"allows specification of which set of properties should be applied in these cases.",
		usedWithin = { CellProperties.class }, standalone = false, tag = "Prevents double labelling after discretization")
public class Exclusion {



	@Label(info = "", tag = "Dominant region label")
	public String winner;

	@Label(info = "", tag = "Suppressed region label - removed from compartments that contain the 'either' label")
	public String loser;




	public String getWinner() {
		return winner;
	}

	public String getLoser() {
		return loser;
	}


}
