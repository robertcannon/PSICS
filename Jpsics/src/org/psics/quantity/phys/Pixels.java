package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Pixels extends PhysicalQuantity {
	 
	
   public Pixels(double d) {
	   super(d, Units.none);
   }

			 
			 
   public Pixels(double d, Units u) {
	   super(d, u, Units.none);
   }

   public Pixels() {
	   this(0., null);
   }

public double getValue() {
	return getNativeValue();
}

}
