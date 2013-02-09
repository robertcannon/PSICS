package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Time extends PhysicalQuantity {


   public Time(double d, Units u) {
	   super(d, u, Units.s);
   }

   public Time() {
	   this(0., null);
   }

public Time makeCopy() {
	 return new Time(value, originalUnits);
}

}
