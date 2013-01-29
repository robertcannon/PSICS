package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class SurfaceCapacitance extends PhysicalQuantity {


   public SurfaceCapacitance(double d, Units u) {
	   super(d, u, Units.uF_per_um2);
   }

   public SurfaceCapacitance() {
	   this(0., null);
   }

   
  
   
}
