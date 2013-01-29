package org.psics.quantity;

import org.psics.quantity.units.Units;

public interface DimensionalQuantity extends DimensionalItem {
	
	public void setValue(double d, Units u);
	
	public void setOriginalText(String s);

	public void setNoValue();
	
}
