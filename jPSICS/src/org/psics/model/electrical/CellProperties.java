package org.psics.model.electrical;

import java.util.ArrayList;
import java.util.HashSet;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.IDable;
import org.psics.be.Standalone;
import org.psics.distrib.DistribExclusion;
import org.psics.distrib.DistribPopulation;
import org.psics.distrib.DistribSpec;
import org.psics.distrib.PopulationConstraint;
import org.psics.model.channel.KSChannel;
import org.psics.model.synapse.Synapse;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.BulkResistivity;
import org.psics.quantity.phys.SurfaceCapacitance;
import org.psics.quantity.units.Units;

@ModelType(info = "Attributes set the membrane capacitance and bulk resitivity; the rest of the " +
		"properties concern channel densities in the membrane.", standalone = true, tag = "Membrane and cytoplasm properties of the cell", usedWithin = {})
public class CellProperties implements AddableTo, Standalone, IDable {

	@Identifier(tag = "identifier for the distribution")
	public String id;

	@Quantity(range = "(0.8, 1.2)", required=false, tag="Membrane capacitance - capacitance per unit area genrally" +
			"around 1 uF per cm2 (equivalent to 0.01 pF per um2)",
			units = Units.uF_per_cm2)
	public SurfaceCapacitance membraneCapacitance;


	@Quantity(range = "(50, 200)", required=false, tag="Resistivity of contentents of the cell, also known as " +
			"axial resistivity",
			units = Units.ohm_cm)
	public BulkResistivity cytoplasmResistivity;

	@Container(contentTypes = {CellRegion.class}, tag = "Regions of the cell containing subpopulations")
	public ArrayList<CellRegion> c_regions = new ArrayList<CellRegion>();


	@Container(contentTypes = {ChannelPopulation.class}, tag = "Channel populations")
	public ArrayList<ChannelPopulation> c_populations = new ArrayList<ChannelPopulation>();

	@Container(contentTypes = {SynapsePopulation.class}, tag = "Synapse populations")
	public ArrayList<SynapsePopulation> c_synapse_populations = new ArrayList<SynapsePopulation>();
	
	@Container(contentTypes = {SingleSynapse.class}, tag = "Synapses positioned one by one")
	public ArrayList<SingleSynapse> c_synapses = new ArrayList<SingleSynapse>();
	
	
	@Container(contentTypes = {DistributionRule.class}, tag = "Distribution rule")
	public ArrayList<DistributionRule> c_rules = new ArrayList<DistributionRule>();


	@Container(contentTypes = {PassiveProperties.class}, tag = "Region specific passive properties")
	public ArrayList<PassiveProperties> c_passprops = new ArrayList<PassiveProperties>();


	@Container(contentTypes = {DensityAdjustment.class}, tag = "Channel density adjustments")
	public ArrayList<DensityAdjustment> c_adjustments = new ArrayList<DensityAdjustment>();


	@Container(contentTypes = {Exclusion.class}, tag = "Either-or labels")
	public ArrayList<Exclusion> c_exclusions = new ArrayList<Exclusion>();




	// TODO make generic?
	public void add(Object obj) {
		if (obj instanceof ChannelPopulation) {
			ChannelPopulation cp = (ChannelPopulation)obj;
			c_populations.add(cp);
		//	E.info("added a pop wit hdist " + cp.distribution + " " + cp.channel + " " +cp.id);
	
		} else if (obj instanceof SynapsePopulation) {
				SynapsePopulation cp = (SynapsePopulation)obj;
				c_synapse_populations.add(cp);
		
		} else if (obj instanceof SingleSynapse) {
			c_synapses.add((SingleSynapse)obj);		
				
		} else if (obj instanceof CellRegion) {
			c_regions.add((CellRegion)obj);

		} else if (obj instanceof DistributionRule) {
			c_rules.add((DistributionRule)obj);

		} else if (obj instanceof PassiveProperties) {
			c_passprops.add((PassiveProperties)obj);

		} else if (obj instanceof DensityAdjustment) {
			c_adjustments.add((DensityAdjustment)obj);

		} else if (obj instanceof Exclusion) {
			c_exclusions.add((Exclusion)obj);

		} else {
			E.error(" cant add " + obj);
		}
	}


	public String getID() {
		return id;
	}

	public void setID(String s) {
		id = s;
	}

	public void resolve() {
		// TODO Auto-generated method stub

	}


	public SurfaceCapacitance getMembraneCapacitance() {
		SurfaceCapacitance ret = membraneCapacitance;
		if (ret == null || ret.iszero()) {
			ret = new SurfaceCapacitance();
			ret.setValue(1., Units.uF_per_cm2);
		}
		return ret;
	}

	public BulkResistivity getBulkResistivity() {
		BulkResistivity ret = cytoplasmResistivity;
		if (ret == null || ret.iszero()) {
			ret = new BulkResistivity();
			ret.setValue(100., Units.ohm_cm);
		}
		return ret;
	}

	  @SuppressWarnings("unused")
	private boolean nonTrivial(String s) {
		boolean ret = false;
		if (s != null && s.trim().length() > 0) {
			ret = true;
		}
		return ret;
	}



	private void addPopulation(DistribSpec ret, Population cp, String match) {
		DistribPopulation bp = cp.makeDistribPopulation();
		ret.addPopulation(bp);

		if (match != null) {
			PopulationConstraint pc = new PopulationConstraint();
			pc.setRestrict();
			pc.setCondition("region=" + match);
			bp.addConstraintFirst(pc);
		}
	}



	private void idCheck1(Population cp, HashSet<String> idHS) {
		String popid = cp.getID();
		if (idHS.contains(popid)) {
			cp.setID(null);
		} else {
			idHS.add(popid);
		}
	}


	private void idCheck2(Population cp, HashSet<String> idHS, String rt) {
		String popid = cp.getID(); 
		if (popid == null) {
			String idbase = cp.getTargetID();
			if (idbase == null || idbase.length() == 0) {
				idbase = rt + "_";
			} else {
				idbase += "_";
			}

			int ic = 0;
			while(idHS.contains(idbase + ic)) {
				ic += 1;
			}
			popid = idbase + ic;
			idHS.add(popid);
			cp.setID(popid);
		}
	}



	


	public DistribSpec getChannelDistributionSpecification() {
		DistribSpec ret = new DistribSpec();


		for (Exclusion exc : c_exclusions) {
			ret.addExclusion(new DistribExclusion(exc.getWinner(), exc.getLoser()));
		}


		// check that all populations have ids;
		HashSet<String> idHS = new HashSet<String>();
		for (Population cp : c_populations) {
			 idCheck1(cp, idHS);
		}
		for (CellRegion cr : c_regions) {
			for (Population cp : cr.populations) {
				idCheck1(cp, idHS);
			}
		}

		for (ChannelPopulation cp : c_populations) {
			 idCheck2(cp, idHS, "chpop");
		}
		for (CellRegion cr : c_regions) {
			for (ChannelPopulation cp : cr.populations) {
				idCheck2(cp, idHS, "chpop");
			}
		}


		for (Population cp : c_populations) {
			addPopulation(ret, cp, null);
		}

		for (CellRegion cr : c_regions) {
			String match = cr.getMatch();
			for (Population cp : cr.populations) {
				addPopulation(ret, cp, match);
			}
		}
		

		if (c_passprops.size() > 0) {
			for (PassiveProperties pp : c_passprops) {
				ret.addLocalPassiveProperties(pp.getRegion(), pp.getResistivity(), pp.getCapacitance());
			}
		}

		if (c_adjustments.size() > 0) {
			for (DensityAdjustment da : c_adjustments) {
				ret.addDensityAdjustment(da.getTargetVoltage(), da.getVariableChannels());
			}
		}

		return ret;
	}

 
	public DistribSpec getSynapseDistributionSpecification() {
		DistribSpec ret = new DistribSpec();
 
		// check that all populations have ids;
		HashSet<String> idHS = new HashSet<String>();
		for (SynapsePopulation cp : c_synapse_populations) {
			 idCheck1(cp, idHS);
		}
	 
		for (SynapsePopulation cp : c_synapse_populations) {
			 idCheck2(cp, idHS, "sy");
		}
		 

		for (SynapsePopulation cp : c_synapse_populations) {
			addPopulation(ret, cp, null);
		}

		for (SingleSynapse ss : c_synapses) {
			DistribPopulation bp = ss.makeDistribPopulation();
			ret.addPopulation(bp);
		}
		
		ret.indexPopulations();
		return ret;
	}



	public ArrayList<KSChannel> getKSChannels() {
		HashSet<String> ids = new HashSet<String>();
		ArrayList<KSChannel> ret = new ArrayList<KSChannel>();

		for (ChannelPopulation cp : c_populations) {
			KSChannel ksc = cp.getKSChannel();
			if (ids.contains(ksc.getID())) {
				// already there;
			} else {
				ids.add(ksc.getID());
				ret.add(ksc);
			}
		}

		for (CellRegion cr : c_regions) {
			for (ChannelPopulation cp : cr.populations) {
				KSChannel ksc = cp.getKSChannel();
				if (ids.contains(ksc.getID())) {
					// already there;
				} else {
					ids.add(ksc.getID());
					ret.add(ksc);
				}
			}
		}
		return ret;
	}

	
 
	public ArrayList<Synapse> getSynapses() {
		HashSet<String> ids = new HashSet<String>();
		ArrayList<Synapse> ret = new ArrayList<Synapse>();

		for (SynapsePopulation cp : c_synapse_populations) {
			Synapse ksc = cp.getSynapse();
			if (ids.contains(ksc.getID())) {
				// already there;
			} else {
				ids.add(ksc.getID());
				ret.add(ksc);
			}
		}

		// TODO could allow synapse populations in a cell region and process them here.  
	 
		
		for (SingleSynapse ss : c_synapses) {
			Synapse ksc = ss.getSynapse();
			if (ids.contains(ksc.getID())) {
				// already there;
			} else {
				ids.add(ksc.getID());
				ret.add(ksc);
			}
		}

		
		return ret;
	}

	
	

	public void setPopulationsFrom(DistribSpec ds) {
		// E.info("setting populations from distrib spec");
		c_populations.clear();
		c_regions.clear();
		c_rules.clear();
		for (DistribPopulation dp : ds.getItems()) {
			ChannelPopulation cp = new ChannelPopulation();
			cp.populateFrom(dp);
			c_populations.add(cp);

		}

	}


	public void addRules(ArrayList<DistributionRule> drs) {
		 c_rules.addAll(drs);
	}


	public void addPops(ArrayList<ChannelPopulation> cpal) {
		c_populations.addAll(cpal);
	}


	public void setSurfaceCapacitance(double d) {
		membraneCapacitance = new SurfaceCapacitance(d, Units.uF_per_cm2);
	}

	public void setAxialResistance(double value) {
		 cytoplasmResistivity = new BulkResistivity(value, Units.ohm_cm);

	}



}
