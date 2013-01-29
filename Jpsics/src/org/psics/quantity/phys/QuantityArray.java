package org.psics.quantity.phys;

import java.util.ArrayList;

import org.psics.quantity.units.DimensionSet;
import org.psics.quantity.units.Units;


public class QuantityArray {

	public double[] values;
	public String[] svalues; // original text representation of quantity;

	public DimensionSet dims;


	private ArrayList<PhysicalQuantity> pvals;



public double[] getValues(DimensionSet ds) {
		double f = dims.getToConversionFactor(ds);
		double[] ret = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			ret[i] = f * values[i];
		}
		return ret;
	}


public void setUnits(Units u) {
	 dims = u.getDimensionSet();

}


public void setValues(double[] v) {
	 values = v;

}

public void setStringValues(String[] sa) {
	svalues = sa;
}

public ArrayList<PhysicalQuantity> getElements() {
		ArrayList<PhysicalQuantity> apq = new ArrayList<PhysicalQuantity>();
		for (int i = 0; i < values.length; i++) {
			double d = values[i];
			PhysicalQuantity pq = new PhysicalQuantity(d, dims);
			if (svalues != null) {
				pq.setOriginalText(svalues[i]);
			}
			apq.add(pq);
		}
		return apq;
}


public int size() {
	if (pvals == null) {
		pvals = getElements();
	}
	return pvals.size();
}

public PhysicalQuantity get(int i) {
	if (pvals == null) {
		pvals = getElements();
	}
	return pvals.get(i);
}




}