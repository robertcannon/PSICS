package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Length extends PhysicalQuantity {


   public Length(double d, Units u) {
	   super(d, u, Units.m);
   }

   public Length() {
	   this(0., null);
   }

   
  
   
}
