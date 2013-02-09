package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class RatePerMole extends PhysicalQuantity {


   public RatePerMole(double d, Units u) {
	   super(d, u, Units.per_ms);
   }

   public RatePerMole() {
	   this(0., Units.per_ms);
   }

}
