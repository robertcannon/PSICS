package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class SurfaceNumberDensityExpression extends PhysicalExpression {


   public SurfaceNumberDensityExpression(String s, Units u) {
	   super(s, u, Units.per_um2);
   }

   public SurfaceNumberDensityExpression() {
	   this("", null);
   }


   public SurfaceNumberDensityExpression makeCopy() {
		 return new SurfaceNumberDensityExpression(expression, originalUnits);
	}
}
