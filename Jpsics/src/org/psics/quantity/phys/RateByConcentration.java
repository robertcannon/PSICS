package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class RateByConcentration extends PhysicalQuantity {


   public RateByConcentration(double d, Units u) {
	   super(d, u, Units.l_per_s_per_mol);
   }

   public RateByConcentration() {
	   this(0., Units.l_per_s_per_mol);
   }

public RateByConcentration makeCopy() {
	 return new RateByConcentration(value, originalUnits);
}

}
