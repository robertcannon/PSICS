package org.psics.quantity.phys;

import org.psics.quantity.units.Units;




public class Temperature extends PhysicalQuantity {

	public Temperature(double d, Units u) {
		super(d, u, Units.K);
	}

	public Temperature() {
		super(273.15, Units.K);
	}



	public Temperature makeCopy() {
		return new Temperature(value, originalUnits);
	}

}
