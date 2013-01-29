package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class Concentration extends PhysicalQuantity {


   public Concentration(double d, Units u) {
	   super(d, u, Units.mol_per_l);
   }

   public Concentration() {
	   this(0., Units.mol_per_l);
   }

public Concentration makeCopy() {
	 return new Concentration(value, originalUnits);
}

}
