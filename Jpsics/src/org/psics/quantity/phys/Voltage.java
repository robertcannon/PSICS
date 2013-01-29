package org.psics.quantity.phys;

import org.psics.quantity.units.DimensionSet;
import org.psics.quantity.units.Units;



public class Voltage extends PhysicalQuantity {


	private Voltage(double d, DimensionSet ds) {
		super(d, ds);
	}



	public Voltage(double d, Units u) {
		super(d, u, Units.V);
	}


	public Voltage() {
		super(0., Units.V);
	}


	public Voltage times(double d) {
		return new Voltage(d * value(), getDimensions());
	}





	public Voltage add(Voltage v) {
		return new Voltage(value() + v.getValue(getDimensions()), getDimensions());
	}



	public Voltage makeCopy() {
		return new Voltage(value, originalUnits);
	}

}
