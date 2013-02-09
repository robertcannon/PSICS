package org.psics.model.control;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.morph.LocalDiscretizationData;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Length;
import org.psics.quantity.units.Units;

@ModelType(info = "The baseElementSize is the main attribute for controlling how the cell morphology is " +
		"compartmentalized. The discretization algorithm is detailed in the 'process' section of the user guide." +
		"The merge flag allow the discretization algorithm to generate exactly the same " +
		"compartmentalizations as certain legacy test cases, but are unlikely to be useful for new models.",
		standalone = false, tag = "Compartmentalization options for the morphology", usedWithin = { PSICSRun.class })
public class StructureDiscretization implements AddableTo {

	@Quantity(range = "(1, 100)", required=false, tag="Default element size. For a segment of radius r, the  " +
			"resulting length, l, is r^(3/2) * baseElementSize.", units = Units.um)
	public Length baseElementSize;


	@Flag(required = false, tag = "If merge is set to false, then it will use at " +
			"least one compartment for each point in the original structure: " +
			"ie, nearby points will not be combined into a single compartment")
	public boolean merge = true;


	public ArrayList<LocalRefinement> c_refinements = new ArrayList<LocalRefinement>();
	

	public void add(Object obj) {
		if (obj instanceof LocalRefinement) {
			c_refinements.add((LocalRefinement)obj);
		} else {
			E.error("can't add " + obj);
		}
	}
	
	
	public void defaultInit() {
		merge = true;
		baseElementSize = new Length(4., Units.um);
	}

	public double getElementSize() {
		double ret = 10.;
		if (baseElementSize != null && baseElementSize.nonzero()) {
			ret = CalcUnits.getLengthValue(baseElementSize);
		}
		return ret;
	}
	
	public ArrayList<LocalDiscretizationData> getLocalDiscs() {
		ArrayList<LocalDiscretizationData> ldd = new ArrayList<LocalDiscretizationData>();
		for (LocalRefinement lr : c_refinements) {
			ldd.add(lr.getLocalDiscretizationData());
		}
		return ldd;
	}

	public boolean getNoGroups() {
		return !merge;
	}



}
