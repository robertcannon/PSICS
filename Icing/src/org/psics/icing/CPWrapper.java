package org.psics.icing;

import java.awt.Color;

import org.catacomb.interlish.structure.Colored;
import org.psics.be.E;
import org.psics.distrib.DistribPopulation;


public class CPWrapper implements Colored {


	DistribPopulation population;


	public CPWrapper(DistribPopulation cp) {
		population = cp;
	}


	public String toString() {
		return population.toString();
	}



	public Color getColor() {
		Color ret = (Color)(population.getCachedColor());
		if (ret == null) {
			String s = population.getColor();
			if (s == null) {
				s = "0xff0000";
			}
			if (!s.toLowerCase().startsWith("0x")) {
				s = "0x" + s;
			}
			try {
				ret = new Color(Integer.decode(s).intValue());
			} catch (Exception ex) {
				E.warning("dodgy color " + s);
				ret = Color.red;
			}
			population.cacheColor(ret);
		}
		return ret;
	}


}
