package org.psics.model.stimrec;

import java.util.Comparator;

import org.psics.num.Compartment;


public class RadiusComparator implements Comparator<Compartment> {

	public int compare(Compartment o1, Compartment o2) {
		 int ret = 0;
		 double r1 = o1.getRadius();
		 double r2 = o2.getRadius();
		 if (r1 < r2) {
			 ret = -1;
		 } else if (r1 > r2) {
			 ret = 1;
		 }
		 return ret;
	}

}
