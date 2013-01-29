package org.psics.quantity.phys;

import org.psics.be.StringValued;
import org.psics.quantity.units.Units;

public class NDValue extends PhysicalQuantity implements StringValued {


   public NDValue(double d) {
	   super(d, Units.none);
   }



   public NDValue(double d, Units u) {
	   super(d, u, Units.none);
   }

   public NDValue() {
	   this(0., null);
   }

public double getValue() {
	return getNativeValue();
}



public NDValue makeCopy() {
	return new NDValue(value);
}

 public String getStringValue() {
	 return "" + value;
 }


}
