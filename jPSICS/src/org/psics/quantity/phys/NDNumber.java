package org.psics.quantity.phys;

import org.psics.be.StringValued;
import org.psics.quantity.units.Units;

public class NDNumber extends IntegerQuantity implements StringValued {


   public NDNumber(int n) {
	   super(n, Units.none);
   }



   public NDNumber(int n, Units u) {
	   super(n, u, Units.none);
   }

   public NDNumber() {
	   this(0, null);
	   hasValue = false;
   }

public int getValue() {
	return getNativeValue();
}



public NDNumber makeCopy() {
	return new NDNumber(value);
}



public String getStringValue() {
	 return "" + value;
}



}
