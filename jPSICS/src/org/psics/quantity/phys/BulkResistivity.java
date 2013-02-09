package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class BulkResistivity extends PhysicalQuantity {


   public BulkResistivity(double d, Units u) {
	   super(d, u, Units.ohm_cm);
   }

   public BulkResistivity() {
	   this(0., null);
   }

   
  
   
}
