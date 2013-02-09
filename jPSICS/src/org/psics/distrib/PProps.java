package org.psics.distrib;

import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.num.CompartmentTree;
import org.psics.num.Compartment;
import org.psics.num.TreeMatcher;
import org.psics.quantity.phys.BulkResistivity;
import org.psics.quantity.phys.SurfaceCapacitance;


public class PProps {

	String region;
	BulkResistivity resistivity;
	SurfaceCapacitance capacitance;


	public PProps(String reg, BulkResistivity r, SurfaceCapacitance c) {
		region = reg;
		resistivity = r;
		capacitance = c;
	}


	public void applyTo(CompartmentTree ctree) {
		double cval = -1.;
		if (capacitance != null) {
			cval = CalcUnits.getSpecificCapacitance(capacitance);
		}
		double rval = -1.;
		if (resistivity != null) {
			rval = CalcUnits.getResistivityValue(resistivity);
		}


		if (region.indexOf("*") < 0) {
		 for (Compartment cpt : ctree.getCompartments()) {
			 if (cpt.labelledWith(region)) {
				 applyToCpt(cpt, rval, cval);
			 }
		 }
		} else {
			TreeMatcher tm = new TreeMatcher(ctree);
			boolean[] ba = tm.getRegionMask(region, TreeMatcher.WHERE);
			int nr = 0;
			int icpt = 0;
			for (Compartment c : ctree.getCompartments()) {
				if (ba[icpt]) {
					nr += 1;
					applyToCpt(c, rval, cval);
				}
				icpt += 1;
			}

			if (nr == 0) {
				E.warning("passive properties " + region + " does not match any regions");
			} else if (nr < 2) {
				E.warning("passive properties " + region + " only matched " + nr + " regions");
			} else {
				// E.info("Set pass props " + region + " on " + nr + " cpts " + rval + " " +cval);
			}
		}

	}


	private void applyToCpt(Compartment cpt, double rval, double cval) {

		 if (rval > 0) {
			 cpt.setResistivity(rval);
		 }
		 if (cval > 0) {
			 cpt.setMembraneCapacitance(cval);
		 }

	}


}
