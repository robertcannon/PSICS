package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class SurfaceNumberDensity extends PhysicalQuantity {


   public SurfaceNumberDensity(double d, Units u) {
	   super(d, u, Units.per_um2);
   }

   public SurfaceNumberDensity() {
	   this(0., null);
   }


   public SurfaceNumberDensity makeCopy() {
		 return new SurfaceNumberDensity(value, originalUnits);
	}
}
