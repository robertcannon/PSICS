package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Capacitance extends PhysicalQuantity {


   public Capacitance(double d, Units u) {
	   super(d, u, Units.F);
   }

   public Capacitance() {
	   this(0., null);
   }

}
