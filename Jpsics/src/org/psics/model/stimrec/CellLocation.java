package org.psics.model.stimrec;

import java.util.ArrayList;
import java.util.Collections;

import org.psics.be.E;
import org.psics.num.Compartment;
import org.psics.num.CompartmentTree;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.StringEnum;
import org.psics.quantity.phys.Length;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;

@ModelType(info = "A means of uniquely picking a location on a cell according to geometrical" +
		"properties of the morphology. If path is specified then alll points" +
		"at that path distance from the soma are ranked according to the ranking criterion" +
		" (currently just radius) and point nearest sequenceFraction is used. If pathFraction is " +
		"supplied instead of path, then it uses that fraction of the maximum path length.",
		standalone = false, tag = "Geometrically defined position on a cell",
		usedWithin = { Access.class })
public class CellLocation {

	@Identifier(tag = "identifier by which this location os referenced from other components")
	public String id = "";

	@Quantity(range = "", required = false, tag = "Path distance from soma", units = Units.um)
	public Length path;

	@Quantity(range="[0, 1]", required = false, tag = "Path distance as a fraction of max path distance",
			units = Units.none)
	public NDValue pathFraction;

	@StringEnum(required = false, tag = "ranking criterion", values = "radius")
	public String rankBy;

	@Quantity(range="[0, 1]", required = false, tag = "Position in ranking of desired point",
		units = Units.none)
	public NDValue sequenceFraction;

	@IntegerNumber(range="[0, n]", required = false, tag = "Ranking order of desired point")
	public NDNumber sequenceIndex;



	public String identifyOn(CompartmentTree ctree) {
		String ret = null;

		ArrayList<Compartment> cpts = new ArrayList<Compartment>();
		ctree.evaluateMetrics();
		if (path != null) {
			cpts = ctree.getCompartmentsAtPathDistance(path);

		} else if (pathFraction != null) {
			Length l = ctree.getMaximumPathDistance();
			l.multiplyBy(pathFraction);
			cpts = ctree.getCompartmentsAtPathDistance(l);

		} else {
			E.error("need path or pathFraction in CellLocation");
		}

		E.info("" + cpts.size() + " compartments at distance " + path);


		Compartment retcpt = null;
		if (cpts.size() == 0) {
			E.error("no components matching location criterion");

		} else if (cpts.size() == 1) {
			retcpt = cpts.get(0);

		} else {
			Collections.sort(cpts, new RadiusComparator());
			int ncpts = cpts.size();
			double df = 1. / (ncpts - 1);
			int ind = 0;
			if (sequenceFraction != null) {
				ind = (int)(Math.round(sequenceFraction.getValue() / df));
			} else if (sequenceIndex != null) {
				ind = sequenceIndex.getValue();
			}

			if (ind < 0) {
				ind = 0;
			} else if (ind >= ncpts) {
				E.shortWarning("request for location index " + ind  + " overruns available " +
						"locations - using " + (ncpts - 1));
				ind = ncpts-1;
			}
			retcpt = cpts.get(ind);
		}

		if (retcpt != null) {
			ret = retcpt.getID();
			if (ret == null) {
				ret = "_" + id;
				retcpt.setID(ret);
			}
		}
		return ret;
	}


}
