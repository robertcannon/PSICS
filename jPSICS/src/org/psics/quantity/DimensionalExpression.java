package org.psics.quantity;

import org.psics.quantity.units.Units;

public interface DimensionalExpression extends DimensionalItem {


	public void setValue(String s, Units u);

	public String getName();

}
