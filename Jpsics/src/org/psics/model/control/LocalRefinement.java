package org.psics.model.control;

import org.psics.morph.LocalDiscretizationData;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Length;
import org.psics.quantity.units.Units;


public class LocalRefinement {

	@Quantity(range = "(1, 100)", required=true, tag="Local element size.", units = Units.um)
	public Length elementSize;
	
	public String from = null;
	public String to = null;
	
	
	public double getElementSize() {
		double ret = 10.;
		if (elementSize != null && elementSize.nonzero()) {
			ret = CalcUnits.getLengthValue(elementSize);
		}
		return ret;
	}


	public LocalDiscretizationData getLocalDiscretizationData() {
		LocalDiscretizationData ldd = new LocalDiscretizationData(from, to, getElementSize());
		return ldd;
	}
	
 
}
