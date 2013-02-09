package org.psics.model.electrical;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.distrib.DistribPopulation;
import org.psics.distrib.PopulationConstraint;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.ModelType;


@ModelType(standalone=false, usedWithin={CellProperties.class},
		tag="A distribution rule for a channel population", info = "For the " +
				"expression, the quantitities that are available are: dendritic diameter [diameter], " +
				"branch order [order], path length from the soma [path] and the geometrical distance from the " +
				"soma [geom], where the names in square brackets are the ones to be used in expressions.")
public class DistributionRule implements AddableTo {

	@Identifier(tag="Identifier (name) for the rule; unique within the model")
	public String id;



	@Container(contentTypes = { RegionMask.class }, tag = "masks for refining restricting target region, can be " +
			"used on thier own or in conjunction with the labels, or expressions.")
	public ArrayList<RegionMask> c_masks = new ArrayList<RegionMask>();


	public DistributionRule() {

	}

	public DistributionRule(String s) {
		id = s;
	}


	public void add(Object obj) {
		if (obj instanceof RegionMask) {
			if (c_masks == null) {
				c_masks = new ArrayList<RegionMask>();
			}
			c_masks.add((RegionMask)obj);
		} else {
			E.error("cant add " + obj);
		}

	}



	public ArrayList<RegionMask> getRegionMasks() {
		if (c_masks == null) {
			c_masks = new ArrayList<RegionMask>();
		}
		return c_masks;
	}

	public void populateFrom(DistribPopulation dp) {
		 if (dp.hasConstraints()) {
			 for (PopulationConstraint dc : dp.getConstraints()) {
				 RegionMask rm = new RegionMask();
				 rm.populateFrom(dc);
				 c_masks.add(rm);
			 }
		 }

	}

	public void setID(String distid) {
		id = distid;
	}


	public void addIncludeRegionMask(String sm) {
		 RegionMask rm = new RegionMask();
		 rm.setIncludeAction();
		 rm.setWhereMatch(sm);
		add(rm);
	}


}
