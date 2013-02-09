package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Current extends PhysicalQuantity {


   public Current(double d, Units u) {
	   super(d, u, Units.A);
   }

   public Current() {
	   this(0., null);
   }

}
