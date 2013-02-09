package org.psics.quantity.phys;

import org.psics.quantity.units.DimensionSet;
import org.psics.quantity.units.Units;



public class Conductance extends PhysicalQuantity {


	private Conductance(double d, DimensionSet ds) {
		super(d, ds);
	}


	public Conductance(double d, Units u) {
		super(d, u, Units.S);
	}

	public Conductance() {
		super(0., Units.S);
	}


	public Conductance times(double d) {
		return new Conductance(d * value(), getDimensions());
	}


	public Conductance add(Conductance v) {
		return new Conductance(value() + v.getValue(getDimensions()), getDimensions());
	}

	public Conductance makeCopy() {
		 return new Conductance(value, originalUnits);
	}
}
