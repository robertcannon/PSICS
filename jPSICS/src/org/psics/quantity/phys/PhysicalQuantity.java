package org.psics.quantity.phys;

import org.psics.be.E;
import org.psics.be.StringValued;
import org.psics.quantity.DimensionalQuantity;
import org.psics.quantity.units.DimensionSet;
import org.psics.quantity.units.Units;



public class PhysicalQuantity implements DimensionalQuantity, StringValued {

	DimensionSet dims;
	double value;

	Units originalUnits;

	String originalText;
	
	boolean hasValue = false;

	public PhysicalQuantity(double d, DimensionSet ds) {
		value = d;
		dims = ds;
		hasValue = true;
	}

	public PhysicalQuantity(double d, Units u) {
		value = d;
		if (u != null) {
			originalUnits = u;
			dims = u.getDimensionSet();
		}
		hasValue = true;
	}

	public PhysicalQuantity(double d, Units u, Units check) {
		this(d, u);

		if (u == null) {
			if (check != null) {
				dims = check.getDimensionSet();
			}
		} else {
			if (u.getDimensionSet().sameDimensionsAs(check)) {
			// OK;
			} else {
				E.error("cant create a " + this + " with units " + u + " - wrong dimensions");
			}
		}
	}

	
	public void setNoValue() {
		hasValue = false;
	}
	
	public boolean valueSet() {
		return hasValue;
	}

	public void multiplyBy(double d) {
		value *= d;
	}


	public void multiplyBy(NDValue ndv) {
		value *= ndv.getValue();
	}


	public String toString() {
		return "" + value + " " + dims;
	}


	public String getName() {
		String cnm = getClass().getName();
		cnm = cnm.substring(cnm.lastIndexOf(".") + 1, cnm.length());
		return cnm;
	}


	public boolean nonzero() {
		return (value != 0.);
	}

	public boolean iszero() {
		return (value == 0.);
	}

	public boolean compatibleWith(PhysicalQuantity pq) {
		return (dims.sameDimensionsAs(pq.getDimensions()));
	}

	public boolean compatibleWith(Units u) {
		return (dims.sameDimensionsAs(u.getDimensionSet()));
	}


	public void setValue(double d, Units u) {
		if (dims == null) {
			dims = u.getDimensionSet();
		}
		if (u == null || u.getDimensionSet() == null) {
			E.error("null dimension set?? " + u);
		}
		if (u.getDimensionSet().sameDimensionsAs(dims)) {
			dims = u.getDimensionSet();
			originalUnits = u;
			value = d;
		} else {
			E.error("cant use units " + u + " for a " + getClass().getName() +  "(" + this + ")");
		}
	}


	public void setValue(PhysicalQuantity pq) {
		if (dims == null) {
			dims = pq.getDimensions();
		}

		if (pq.getDimensions() == null) {
			E.error("got pq with null dims? " + pq);
		}

		if (pq.getDimensions().sameDimensionsAs(dims)) {
			dims = pq.getDimensions();
			value = pq.value;
		}
	}



	public double value() {
		return value;
	}


	public DimensionSet getDimensions() {
		return dims;
	}


	public double getValue(Units u) {
		return getValue(u.getDimensionSet());
	}

	public double getValue(DimensionSet ds) {
		double f = dims.getToConversionFactor(ds);
		return value * f;
	}


	public PhysicalQuantity times(PhysicalQuantity pq) {
		return new PhysicalQuantity(value * pq.value(), dims.times(pq.getDimensions()));
	}

	public double getNativeValue() {
		return value;
	}

	public void incrementNative(double d) {
		value += d;
	}
	
	public void setOriginalText(String s) {
		originalText = s;
	}

	public String getOriginalText() {
		return originalText;
	}

	public String getStringValue() {
		 String ret = "" + value;
		 if (originalUnits != null) {
			 ret += originalUnits.getName();
		 }
		 return ret;
	}


}
