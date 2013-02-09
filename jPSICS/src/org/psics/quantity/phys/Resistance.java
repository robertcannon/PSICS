package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Resistance extends PhysicalQuantity {


   public Resistance(double d, Units u) {
	   super(d, u, Units.ohm);
   }

   public Resistance() {
	   this(0., null);
   }

}
