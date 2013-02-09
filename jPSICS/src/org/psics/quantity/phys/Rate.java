package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Rate extends PhysicalQuantity {


   public Rate(double d, Units u) {
	   super(d, u, Units.per_ms);
   }

   public Rate() {
	   this(0., Units.per_ms);
   }


   public Rate makeCopy() {
	   return new Rate(value, originalUnits);
   }


}
