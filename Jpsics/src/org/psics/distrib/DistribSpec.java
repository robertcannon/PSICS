package org.psics.distrib;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.psics.be.E;
import org.psics.be.KeyedList;
import org.psics.be.RandomNumberGenerator;
import org.psics.num.Compartment;
import org.psics.num.CompartmentTree;
import org.psics.num.math.MersenneTwister;
import org.psics.quantity.phys.BulkResistivity;
import org.psics.quantity.phys.SurfaceCapacitance;
import org.psics.quantity.phys.Voltage;
import org.psics.num.model.synapse.SypopData;

public class DistribSpec {

	public final static int CHANNEL = 1;
	public final static int SYNAPSE = 2;

	KeyedList<DistribPopulation> populations = new KeyedList<DistribPopulation>();

	ArrayList<DistribExclusion> exclusions = new ArrayList<DistribExclusion>();

	
	// these two could go elsewhere
	ArrayList<PProps> ppropsAL = new ArrayList<PProps>();

	ArrayList<ChannelBalance> cbalAL = new ArrayList<ChannelBalance>();



	public void addPopulation(DistribPopulation dpop) {
		populations.add(dpop);
	}


	public void addExclusion(DistribExclusion de) {
		exclusions.add(de);
	}


	private HashSet<String> makeIDHS() {
		HashSet<String> ret = new HashSet<String>();
		for (DistribPopulation dp : populations.getItems()) {
			ret.add(dp.getID());
		}
		return ret;
	}


	public ArrayList<DistribPopulation> getItems() {
		ArrayList<DistribPopulation> ret = new ArrayList<DistribPopulation>();
		ret.addAll(populations.getItems());
		// E.info("returning list of " + ret.size());
		return ret;
	}


	public void applyExclusions(PointTree chtree) {

		PointTreeMatcher ctm = new PointTreeMatcher(chtree);
		for (DistribExclusion dex : exclusions) {
			ctm.applyExclusion(dex.getWinner(), dex.getLoser());
		}
	}
	
	
	public void passiveInit(PointTree pointTree, CompartmentTree ctree) {
		ctree.evaluateMetrics();
		for (PProps pp : ppropsAL) {
			pp.applyTo(ctree);
		}
		ctree.indexSource(pointTree.size());
	}



	public void populate(PointTree pointTree, CompartmentTree ctree, RandomNumberGenerator rng,
			boolean squareCaps, int popType) {

		applyExclusions(pointTree);

		ArrayList<PointPopulation> chpops = new ArrayList<PointPopulation>();
		for (DistribPopulation dp : populations.getItems()) {
			PointPopulation chpop = new PointPopulation(dp);
			chpops.add(chpop);
			RandomNumberGenerator urng = rng;
			if (dp.hasSeed()) {
				urng = new MersenneTwister(dp.getSeed());
			}
			chpop.realize(pointTree, urng, squareCaps);
		}

		// now we've got the channel positions - need to map them to compartments
		for (PointPopulation chp : chpops) {
			int[] nch = new int[ctree.size()];
			float[][][] cpos = chp.getPositions();
			for (int i = 0; i < cpos.length; i++) {
				if (cpos[i] != null && cpos[i].length > 0) {
					// find the compartment the point i from the channel tree is in
					// get path length of its proximal boundary

					Compartment cpt = ctree.getCompartmentForTreePoint(i);
					double proxPL = cpt.getProxPathLength();
					int icpt = cpt.getIndex();

					Compartment ptry = null;
					for (int j = cpos[i].length - 1; j >= 0;  j--) {
						double pc = cpos[i][j][3];


						if( pc >= proxPL) {
							nch[icpt] += 1;
							// in the compartment

							/* For finely digitized trees, we're likely to be in the compartment
							 * but for coarser ones we could be anywhere between the the compartment
							 * containing the original segment end point and the one containing the
							 * parent of that point, which could be several compartments up in the
							 * ctree (or even hundreds if the original was just a cable defined
							 * by its ends)
							 *
							 */

						} else if (ptry != null && pc > ptry.getProxPathLength()) {
							// MUSTDO verify that ptry is always the right choice
							//( ie, having stepped back to ptry, we never need to step forward)
							nch[ptry.getIndex()] += 1;

						} else {
							Compartment p = cpt.getParent();
							while (p != null && pc <= p.getProxPathLength()) {
								p = p.getParent();
							}
							if (p == null) {
								E.warning("cant find compartment with prox path below " + pc);
							} else {
								ptry = p;
								nch[p.getIndex()] += 1;
							}
							// try parents till we get it
						}

					}
				}
			}

			for (int i = 0; i < nch.length; i++) {
				if (nch[i] > 0) {
					Compartment cpt = ctree.getCompartment(i);
					if (popType == CHANNEL) {
						cpt.addChannels(chp.getTypeID(), nch[i]);
					} else if (popType == SYNAPSE) {
						cpt.addSynapses(chp.getID(), chp.getTypeID(), nch[i]);
					}
				}
			}
		}
	}


    // a quicker alternative to putting channels on the cell and then allocating to compartments;
	public void populateCompartments(CompartmentTree ctree, RandomNumberGenerator rng) {
		ctree.checkMetrics();
		for (DistribPopulation dp : populations.getItems()) {
			dp.populate(ctree, rng);
		}
	}




	public ArrayList<ChannelBalance> getChannelBalances() {
		return cbalAL;
	}



	public void addLocalPassiveProperties(String region, BulkResistivity r, SurfaceCapacitance c) {
		ppropsAL.add(new PProps(region, r, c));
	}



	public void addDensityAdjustment(Voltage targetVoltage, String[] variableChannels) {
		cbalAL.add(new ChannelBalance(targetVoltage, variableChannels));
	}




	public DistribPopulation getPopulation(String s) {
		DistribPopulation ret = null;

		  if (populations.hasItem(s)) {
			ret = populations.getItem(s);
		}
		return ret;
	}


	private String newID() {
		HashSet<String> idHS = makeIDHS();
		int ip = 1;
		while (idHS.contains("population_" + ip)) {
			ip += 1;
		}
		return "population_" + ip;
	}




	public DistribPopulation newPopulation() {
		String id = newID();

		DistribPopulation dp = new DistribPopulation(id, "");
		populations.add(dp);
		return dp;
	}




	public void remove(DistribPopulation bp) {
		populations.remove(bp);
	}



	public void moveUp(DistribPopulation p) {
		populations.moveUp(p);
	}

	public void moveDown(DistribPopulation p) {
		populations.moveDown(p);
	}


	public boolean hasSynapses() {
		boolean ret = false;
		if (populations.size() > 0) {
			ret = true;
		}
		return ret;
	}


	public void indexPopulations() {
		int idx = 0;
		for (DistribPopulation dp : populations.getItems()) {
			dp.setNumID(idx);
			idx += 1;
		 }
		
	}

 


	public SypopData getSypopData(HashMap<String, Integer> synapseNumIDs) {
		SypopData spd = new SypopData(populations.size());
		for (DistribPopulation dp : populations.getItems()) {
			String tid = dp.getTypeID();
			int tnid = synapseNumIDs.get(tid);
			spd.addPopulation(dp.getNumID(), dp.getID(), dp.getTypeID(), tnid);
		}
		return spd;
	}

}
