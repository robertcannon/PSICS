package org.psics.quantity.phys;

import org.psics.be.E;
import org.psics.be.StringValued;
import org.psics.quantity.DimensionalQuantity;
import org.psics.quantity.units.DimensionSet;
import org.psics.quantity.units.Units;



public class IntegerQuantity implements DimensionalQuantity, StringValued {

	DimensionSet dims;
	int value;

	Units originalUnits;

	String originalText;
	
	boolean hasValue = false;

	public IntegerQuantity() {
		value = 0;
		dims = null;
		hasValue = false;
	}

	public IntegerQuantity(int n, DimensionSet ds) {
		value = n;
		dims = ds;
		hasValue = true;
	}

	public IntegerQuantity(int n, Units u) {
		value = n;
		if (u != null) {
			originalUnits = u;
			dims = u.getDimensionSet();
		}
		hasValue = true;
	}

	public IntegerQuantity(int n, Units u, Units check) {
		this(n, u);

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
		value = 0;
		hasValue = false;
	}
	
	
	public boolean valueSet() {
		return hasValue;
	}
	
	public void multiplyBy(int n) {
		value *= n;
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
		return (value != 0);
	}

	public boolean iszero() {
		return (value == 0);
	}


	public void setIntValue(int n, Units uin) {
		Units u = uin;
		if (u == null) {
			u = Units.none;
		}
		if (dims == null) {
			dims = u.getDimensionSet();
			value = n;
			hasValue = true;
			
		} else if (u.getDimensionSet().sameDimensionsAs(dims)) {
			dims = u.getDimensionSet();
			originalUnits = u;
			value = n;
			hasValue = true;
			
		} else {
			E.error("cant use units " + u + " for a " + getClass().getName() +  "(" + this + ")");
		}
	}


	public void setValue(IntegerQuantity pq) {
		if (pq.getDimensions().sameDimensionsAs(dims)) {
			dims = pq.getDimensions();
			value = pq.value;
			hasValue = true;
		}
	}



	public int value() {
		return value;
	}


	public DimensionSet getDimensions() {
		return dims;
	}


	public double getValue(Units u) {
		return getValue(u.getDimensionSet());
	}

	public int getValue(DimensionSet ds) {
		int f = (int)(Math.round(dims.getToConversionFactor(ds))); // POSERR use longs?
		return value * f;
	}


	public IntegerQuantity times(IntegerQuantity pq) {
		return new IntegerQuantity(value * pq.value(), dims.times(pq.getDimensions()));
	}

	public int getNativeValue() {
		return value;
	}

	public void setOriginalText(String s) {
		originalText = s;
	}

	public String getOriginalText() {
		return originalText;
	}

	public void setValue(double d, Units u) {
		 setIntValue((int)(Math.round(d)), u);
	}

	public String getStringValue() {
		 String ret = "" + value;
		 if (originalUnits != null && originalUnits != Units.none) {
			 ret += originalUnits.getName();
		 }
		 return ret;
	}
}
