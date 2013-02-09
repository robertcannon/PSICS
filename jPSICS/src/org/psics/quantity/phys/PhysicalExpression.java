package org.psics.quantity.phys;

import org.psics.be.E;
import org.psics.be.StringValued;
import org.psics.quantity.DimensionalExpression;
import org.psics.quantity.units.DimensionSet;
import org.psics.quantity.units.Units;



public class PhysicalExpression implements DimensionalExpression, StringValued {

	DimensionSet dims;
	String expression;

	String originalText;
	Units originalUnits;


	public PhysicalExpression(String s, DimensionSet ds) {
		expression = s;
		dims = ds;
	}

	public PhysicalExpression(String s, Units u) {
		expression = s;
		if (u != null) {
			originalUnits = u;
			dims = u.getDimensionSet();
		}
	}

	public PhysicalExpression(String s, Units u, Units check) {
		this(s, u);

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


	public String toString() {
		return expression + " " + dims;
	}


	public String getName() {
		String cnm = getClass().getName();
		cnm = cnm.substring(cnm.lastIndexOf(".") + 1, cnm.length());
		return cnm;
	}




	public boolean compatibleWith(PhysicalExpression pq) {
		return (dims.sameDimensionsAs(pq.getDimensions()));
	}

	public boolean compatibleWith(Units u) {
		return (dims.sameDimensionsAs(u.getDimensionSet()));
	}


	public void setValue(String s, Units u) {
		if (dims == null) {
			dims = u.getDimensionSet();
		}

		if (u.getDimensionSet().sameDimensionsAs(dims)) {
			dims = u.getDimensionSet();
			originalUnits = u;
			expression = s;
		} else {
			E.error("cant use units " + u + " for a " + getClass().getName() +  "(" + this + ")");
		}
	}







	public DimensionSet getDimensions() {
		return dims;
	}





	public void setOriginalText(String s) {
		originalText = s;
	}

	public String getOriginalText() {
		return originalText;
	}

	public String getStringValue() {
		 String ret = expression;
		 if (originalUnits != null) {
			// ret += originalUnits.getName();
		 }
		 return ret;
	}

	public void setValue(PhysicalQuantity pq) {
		dims = pq.getDimensions();
		expression = pq.getOriginalText();
	// 	E.info("set expression to " + expression + " from " + pq);
	}


}
