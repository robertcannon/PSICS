package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class AnyQuantity extends PhysicalQuantity {


   public AnyQuantity(double d, Units u) {
	   super(d, u, Units.any);
   }

   public AnyQuantity() {
	   this(0., Units.any);
   }

public AnyQuantity makeCopy() {
	 return new AnyQuantity(value, originalUnits);
}

	public void setValue(double d, Units u) {
		dims = u.getDimensionSet();
		super.setValue(d, u);
	}
}
