package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class PhysicalCoordinate extends PhysicalQuantity {


   public PhysicalCoordinate(double d, Units u) {
	   super(d, u, Units.m);
   }

   public PhysicalCoordinate() {
	   this(0., Units.um);
   }

   
  
   
}
