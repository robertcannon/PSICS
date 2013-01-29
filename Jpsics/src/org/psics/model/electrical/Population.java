package org.psics.model.electrical;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.distrib.DistribPopulation;
import org.psics.distrib.PopulationConstraint;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Expression;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.StringEnum;
import org.psics.quantity.phys.IntegerQuantity;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.phys.SurfaceNumberDensity;
import org.psics.quantity.phys.SurfaceNumberDensityExpression;
import org.psics.quantity.units.Units;


public abstract class Population implements AddableTo {

	@Identifier(tag = "optional identifier for use if access is needed to the parameters of the population")
	public String id;
	
	
	@Label(info = "Color to use when displaying population in channel tool", tag = "hex color")
	public String color;
	
	@Expression(required = false, tag = "Density expression, can include path length, p, "
			+ "branch order b, radius r and distance from the soma d", units = Units.per_um2)
	public SurfaceNumberDensityExpression density;
	
	@Container(contentTypes = { RegionMask.class }, tag = "masks for refining restricting target region, can be "
			+ "used on thier own or in conjunction with the labels, or expressions.")
	public ArrayList<RegionMask> c_masks = new ArrayList<RegionMask>();
	
	@ReferenceByIdentifier(tag = "The distribution rule for the channel", targetTypes = { DistributionRule.class }, required = false, location = Location.local)
	public String distribution;
	public DistributionRule r_distribution;
	
	@Quantity(range = "[0,100)", required = false, tag = "Maximum density", units = Units.per_um2)
	public SurfaceNumberDensity maxDensity;
	
	@IntegerNumber(range = "[0, 10^6)", required = false, tag = "Total number of channels in the population")
	public IntegerQuantity totalNumber;
	
	@StringEnum(required = false, tag = "Allocation rule for individual channels from the evealuated local density", values = "Regular, Poission")
	public String allocation;
	
	public NDNumber seed = new NDNumber();
	// public long seed = (long)(1.e6 * Math.random());
	
	@ReferenceByIdentifier(location = Location.local, required = false, tag = "Define density relative to another population. Either this, or the density "
			+ "expression can be supplied but not both", targetTypes = { ChannelPopulation.class })
	public String relativeTo;
	public Population r_relativeTo;
	
	@Quantity(range = "[0,10)", required = false, tag = "factor relative to target population. "
			+ "Only applies if the relativeTo attribute is set", units = Units.none)
	public NDValue densityFactor;
	
	 
	
	public String getID() {
		 return id;
	 }

	public void setID(String s) {
		 id = s;
	 }
	
	
	public void add(Object obj) {
		 if (obj instanceof RegionMask) {
			 c_masks.add((RegionMask)obj);

		 } else if (subAdd(obj)) {
			 // OK
		 } else {
			 E.warning("cant add " + obj);
		 }
	 }

	
	public abstract boolean subAdd(Object obj);
	
	public String getDensityExpression() {
		return density.getStringValue();
	}

	public ArrayList<RegionMask> getRegionMasks() {
		// E.info("distribution rule: " + distribution + "  r_dist..=" + r_distribution);
	
		ArrayList<RegionMask> ret = null;
		if (r_distribution != null) {
			ret = r_distribution.getRegionMasks();
		}
		return ret;
	}

	public String getColor() {
		return color;
	}

	public void setDensityExpression(String s) {
		 density = new SurfaceNumberDensityExpression(s, Units.per_um2);
	}

	public void setDistribution(String s) {
		distribution = s;
	}
	
	public abstract String getTargetID();

	public DistribPopulation makeDistribPopulation() {
		DistribPopulation ret = new DistribPopulation(id, getTargetID());
	
		if (relativeTo != null) {
			double df = 1.;
			if (densityFactor != null) {
				df = densityFactor.getValue();
			}
			ret.setRelativeDensity(df, relativeTo);
	
		} else if (density != null) {
			ret.setDensityExpression(density.getStringValue());
		} else {
			ret.setDensityExpression("1.");
		}
	
		ret.setColor(color);
		ret.setSeed(seed.getValue());
	
		if (maxDensity != null) {
			ret.setMaxDensity(CalcUnits.getReciprocalArea(maxDensity));
		}
	
		if (totalNumber != null) {
			ret.setTotalNumber(totalNumber.getNativeValue());
		}
	
		if (allocation != null) {
			if (allocation.toLowerCase().equals("regular")) {
				ret.setAllocationRegular();
			} else if (allocation.toLowerCase().equals("poisson")) {
				ret.setAllocationPoisson();
			} else {
				E.warning("unrecoginized " + allocation);
			}
		}
	
		 if (r_distribution != null) {
			c_masks.addAll(r_distribution.getRegionMasks());
			distribution = null;
			r_distribution = null;
		}
	
		 for (RegionMask rm : c_masks) {
		 	PopulationConstraint pc = new PopulationConstraint();
	
				String sa = rm.getAction();
				if (sa.equals("include")) {
					pc.setInclude();
	
				} else if (sa.equals("exclude")) {
					pc.setExclude();
	
				} else if (sa.startsWith("restrict")) {
					pc.setRestrict();
	
				} else {
					E.error("unrecognized mask action " + sa);
				}
				String cond = rm.getCondition();
				pc.setCondition(cond);
	
				ret.addConstraint(pc);
		}
		return ret;
	}

	
	public abstract void setTargetID(String sid);
	
	public void populateFrom(DistribPopulation dp) {
	
		id = dp.getID();
		setTargetID(dp.getTypeID());
	
		if (dp.isRegular()) {
			allocation = "Regular";
		} else {
			allocation = "Poisson";
		}
	
		color = dp.getColor();
		// E.info("populationg a cp wit hcolor " + color);
	
		if (dp.hasSeed()) {
			seed.setIntValue(dp.getSeed(), null);
		} else {
			seed.setNoValue();
		}
	
		if (dp.isRelative()) {
			relativeTo = dp.getRelTarget();
			densityFactor = new NDValue(dp.getRelFactor());
	
		} else {
			density = new SurfaceNumberDensityExpression(dp.getExpression(), Units.per_um2);
		}
	
	
		if (dp.getFixTotal()) {
			totalNumber = new IntegerQuantity(dp.getTotalNumber(), Units.none);
		} else {
			totalNumber = null;
		}
	
		if (dp.getCapDensity()) {
			maxDensity = new SurfaceNumberDensity(dp.getMaxDensity(), Units.per_um2);
		} else {
			maxDensity = null;
		}
	
	
		if (dp.hasConstraints()) {
	
			 for (PopulationConstraint dc : dp.getConstraints()) {
				 RegionMask rm = new RegionMask();
				 rm.populateFrom(dc);
				 c_masks.add(rm);
			 }
		}
	
	}

}
