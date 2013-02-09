package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Charge extends PhysicalQuantity {


   public Charge(double d, Units u) {
	   super(d, u, Units.e);
   }

   public Charge() {
	   this(0., null);
   }

public Charge makeCopy() {
	 return new Charge(value, originalUnits);
}

}
