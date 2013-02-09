package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Frequency extends PhysicalQuantity {


   public Frequency(double d, Units u) {
	   super(d, u, Units.Hz);
   }

   public Frequency() {
	   this(0., Units.Hz);
   }


   public Frequency makeCopy() {
	   return new Frequency(value, originalUnits);
   }


}
