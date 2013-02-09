package org.psics.quantity.phys;

import org.psics.quantity.units.Units;

public class ByVoltage extends PhysicalQuantity {


   public ByVoltage(double d, Units u) {
	   super(d, u, Units.per_mV);
   }

   public ByVoltage() {
	   this(0., Units.per_mV);
   }

}
