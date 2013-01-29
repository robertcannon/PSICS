package org.psics.distrib;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.num.CalcUnits;
import org.psics.num.Compartment;
import org.psics.num.CompartmentTree;
import org.psics.num.TreeMatcher;
import org.psics.quantity.Evaluator;
import org.psics.quantity.phys.SurfaceNumberDensity;
import org.psics.quantity.units.Units;

import org.psics.be.E;
import org.psics.be.IDd;
import org.psics.be.RandomNumberGenerator;

public class DistribPopulation implements IDd {

	String id;
	int numid;
	
	String typeID;

	String expression;

	boolean capDensity = false;
	SurfaceNumberDensity maxDensity;

	boolean relDefined = false;
	double relFactor;
	String relTarget;

	boolean fixTotal = false;
	int totalNumber;

	String color;

	public final static int REGULAR = 0;
	public final static int POISSON = 1;

	int allocation;

	int seed = -1;


	ArrayList<PopulationConstraint> constraints = new ArrayList<PopulationConstraint>();

	Object cachedColor;
	boolean changed;

	DistribPopulation relTargetPop;

	String targetPointID = null;
	

	public DistribPopulation(String popid, String cid) {
		id = popid;
		typeID = cid;
	}



	public void setRelatoveDensity(double d, String s) {
		relDefined = true;
		relFactor = d;
		relTarget = s;
	}


	public void setFixTotal(boolean b) {
		fixTotal = b;
	}

	public void setTotalNumber(int n) {
		fixTotal = true;
		totalNumber = n;
	}


	public void setCapDensity(boolean b) {
		capDensity = b;
	}

	public boolean getCapDensity() {
		return capDensity;
	}

	public void setMaxDensity(double d) {
		maxDensity = new SurfaceNumberDensity(d, Units.per_um2);
		capDensity = true;
	}


	public boolean getFixTotal() {
		return fixTotal;
	}

	public double getMaxDensity() {
		return CalcUnits.getReciprocalArea(maxDensity);
	}

	public int getTotalNumber() {
		return totalNumber;
	}

	public void setSeed(int ls) {
		seed = ls;
	}

	public boolean hasSeed() {
		return (seed >= 0);
	}
	
	public int getSeed() {
		return seed;
	}

	public String getID() {
		return id;
	}

	public String toString() {
		return id;
	}


	public void setTypeID(String s) {
		typeID = s;
	}

	public String getTypeID() {
		return typeID;
	}

	public String getDensityExpression() {
		return expression;
	}


	public void setAllocationRegular() {
		allocation = REGULAR;
	}

	public void setAllocationPoisson() {
		allocation = POISSON;
	}

	public boolean isRegular() {
		boolean ret = false;
		if (allocation == REGULAR) {
			ret = true;
		}
		return ret;
	}


	public void removeConstraint(PopulationConstraint ctrt) {
		constraints.remove(ctrt);
	}



	public ArrayList<PopulationConstraint> getConstraints() {
		return constraints;
	}



	// OPTIONS
	// Three possible flavours of populate: this one matches the total number of items  (
	// channels or synapses) to the nearest one.
	// could also randomly round the number of items on each compartment, giving a larger variance
	// but no compartment order dependence
	// or could use a gaussian rv for each compartment to generate the actual number of items.
	public void populate(CompartmentTree ctree, RandomNumberGenerator rng) {

		 // the subclass evaluates the (floating point) number of itemss per segment
		 // here we handle masking, integer allocation and treatment of remainders (same for all subclasses)
		
		double[] rnchs = null;
		if (fixTotal && totalNumber == 1 && targetPointID != null) {
			rnchs = getSingleItemRNChans(ctree);
		} else {
			rnchs = getRNChans(ctree);
		}


		 double credit = 0.;
		 Compartment[] cpts = ctree.getCompartments();
		 int ntot = 0;
		 for (int i = 0; i < cpts.length; i++) {
			 credit += rnchs[i];
			 int nch = 0;
			 if (credit > 0.) {

				 nch = (int)(Math.floor(credit));
				 if (rng.random() < (credit - nch)) {
					 nch += 1;
				 }
				 credit -= nch;

				 if (nch > 0) {
					 cpts[i].addChannels(typeID, nch);
					 ntot += nch;
				 }
			 }

		 }
	}


	public PopulationConstraint newConstraint() {
		PopulationConstraint pc = new PopulationConstraint(PopulationConstraint.NONE, "");
		constraints.add(pc);
		return pc;
	}


	public void addConstraint(PopulationConstraint pc) {
		constraints.add(pc);
	}

	public void addConstraintFirst(PopulationConstraint pc) {
		constraints.add(0, pc);
	}



	public void addConstraint() {
		constraints.add(new PopulationConstraint(PopulationConstraint.NONE, ""));
	}


	public void setTargetPointID(String sid) {
		targetPointID = sid;
	}
	
	

	private void applyMasks(CompartmentTree ctree, double[] rnchs) {

		TreeMatcher matcher = new TreeMatcher(ctree);

		// if the first constraint is an include, then we exclude everything by default
		// otherwise we include everything
		boolean bsv = true;
		if (constraints.size() > 0 && constraints.get(0).isInclude()) {
			bsv = false;
		}

		boolean[] bincl = new boolean[rnchs.length];
		for (int i = 0; i < bincl.length; i++) {
			bincl[i] = bsv;
		}


		for (PopulationConstraint pc : constraints) {
			boolean[] bwk = null;
			if (pc.isRegion()) {
				bwk = matcher.getRegionMask(pc.getRegion(), pc.getRegionMode());

			} else {
				bwk = getMask(ctree, pc.getCondition());
			}

			int nin = 0;
			for (int i = 0; i < bwk.length; i++) {
				if (bwk[i]) {
					nin++;
				}
			}

			if (pc.isInclude()) {
				for (int i = 0; i < bwk.length; i++) {
					bincl[i] = (bincl[i] || bwk[i]);
				}

			} else if (pc.isExclude()) {
				for (int i = 0; i < bwk.length; i++) {
					bincl[i] = (bincl[i] && ! bwk[i]);
				}

			} else if (pc.isRestrict()) {
				for (int i = 0; i < bwk.length; i++) {
					bincl[i] = (bincl[i] && bwk[i]);
				}
			}
		}

		for (int i = 0; i < rnchs.length; i++) {
			if (!bincl[i]) {
				rnchs[i] = 0.;
			}
		}

	}


	private boolean[] getMask(CompartmentTree ctree, String cond) {
		ctree.checkMetrics();
		boolean[] ba = new boolean[ctree.size()];
		HashMap<String, Double> varHM = new HashMap<String, Double>();
		ctree.getRootCompartment().exportVariables(varHM);

		Evaluator eval = new Evaluator(cond, varHM);
		if (eval.valid()) {
			int icpt = 0;
			for (Compartment cpt : ctree.getCompartments()) {
				cpt.exportVariables(varHM);
				ba[icpt] = eval.getBoolean(varHM);
				icpt += 1;
			}
		} else {
			E.error("invalid expression: " + eval);
		}
		return ba;
	}



	public void setRelTargetPopulation(DistribPopulation dp) {
		relTargetPop = dp;
	}




	public double[][] getDensities(PointTree chTree) {
		return getDensities(chTree, 0);
	}

	public double[][] getDensities(PointTree chTree, int reclevel) {
		double[][] ret = new double[0][0];

		boolean isrel = false;
		if (isRelative()) {
			isrel = true;
			if (reclevel > 10) {
				E.error("Cycle detected in relative density definition");

			} else if (relTargetPop != null) {
				ret = relTargetPop.getDensities(chTree, reclevel + 1);

			} else {
				E.oneLineWarning("cant get relative densities - no population " + relTarget);
			}
			if (relFactor == 0) {
				E.warning("relative density factor is zero - no points allocated");
			}

		} else {
			ret = getLocalDensities(chTree);
		}
		double maxval = 0.;
			if (capDensity) {
				maxval = CalcUnits.getReciprocalArea(maxDensity);
			}
			for (double[] da : ret) {
				for (int i = 0; i < da.length; i++) {
					if (isrel) {
						da[i] *= relFactor;
					}
					if (capDensity && da[i] > maxval) {
						da[i] = maxval;
					}
				}
		}
		return ret;
	}



	private double[][] getLocalDensities(PointTree chTree) {
		double maxden = 0.;

		double[][] divrad = chTree.getDivisionEndRadii();
		int[] divbo = chTree.getDivBranchOrder();
		double[][] divpl = chTree.getDivPathLength();
		int np = divpl.length;

		double[][] ret = new double[divpl.length][];
		boolean[][] bin = new boolean[divpl.length][];


		boolean bsv = true;
		if (constraints.size() > 0 && constraints.get(0).isInclude()) {
			bsv = false;
		}

		for (int i = 0; i < bin.length; i++) {
			boolean[] abin = new boolean[divpl[i].length];
			for (int j = 0; j < abin.length; j++) {
				abin[j] = bsv;
			}
			bin[i] = abin;
		}

		PointTreeMatcher matcher = new PointTreeMatcher(chTree);
		for (PopulationConstraint pc : constraints) {
			boolean isincl = pc.isInclude();
			boolean isexcl = pc.isExclude();
			boolean isrest = pc.isRestrict();
			if (pc.isRegion()) {
				int nin = 0;
				boolean[] bwk = matcher.getRegionMask(pc.getRegion(), pc.getRegionMode());
				for (int i = 0; i < bwk.length; i++) {
					for (int j = 0; j < bin[i].length; j++) {
						if (isincl) {
							bin[i][j] = (bin[i][j] || bwk[i]);

						} else if (isexcl) {
							bin[i][j] = (bin[i][j] && ! bwk[i]);

						} else if (isrest) {
							bin[i][j] = (bin[i][j] && bwk[i]);
						}
						if (bin[i][j]) {
							nin += 1;
						}
					}
				}
				// E.info("after region mask " + pc.getRegion() + pc.getRegionMode() + " " +  nin);

			} else {
				boolean[][] bwk = new boolean[np][];
				String expr = pc.getCondition();
				HashMap<String, Double> varHM = new HashMap<String, Double>();
				varHM.put("r", new Double(0));
				varHM.put("p", new Double(0));
				varHM.put("d", new Double(0));
				varHM.put("bo", new Double(0));
				Evaluator eval = new Evaluator(expr, varHM);

				if (eval.valid()) {
					int nin = 0;
					for (int i = 0; i < np; i++) {
						int na = divpl[i].length;
						boolean[] ad = new boolean[na];
						bwk[i] = ad;
						for (int j = 0; j < na; j++) {
							varHM.put("r", new Double(divrad[i][j]));
							varHM.put("d", new Double(2 * divrad[i][j]));
							varHM.put("p", new Double(divpl[i][j]));
							varHM.put("bo", new Double(divbo[i]));
							boolean b = eval.getBoolean(varHM);

							if (isincl) {
								bin[i][j] = (bin[i][j] || b);

							} else if (isexcl) {
								bin[i][j] = (bin[i][j] && ! b);

							} else if (isrest) {
								bin[i][j] = (bin[i][j] && b);
							}
							if (bin[i][j]) {
								nin += 1;
							}
						}
					}
					// E.info("after expressin region " + expr + " " + nin);
				}
			}
		}




		int nnz = 0;

		if (expression == null || expression.trim().length() == 0) {
			for (int i = 0; i < np; i++) {
				int na = divpl[i].length;
				double[] ad = new double[na];
				for (int j = 0; j < na; j++) {
					if (bin[i][j]) {
						ad[j] = 1.;
						if (maxden < 1.) {
							maxden = 1.;
						}

					} else {
						ad[j] = 0.;
					}
				}
				ret[i] = ad;
			}


		} else {
			HashMap<String, Double> varHM = new HashMap<String, Double>();
			varHM.put("r", new Double(0));
			varHM.put("p", new Double(0));
			varHM.put("d", new Double(0));
			varHM.put("bo", new Double(0));
			Evaluator eval = new Evaluator(expression, varHM);

			int ntot = 0;
			if (eval.valid()) {
				for (int i = 0; i < np; i++) {
					int na = divpl[i].length;
					double[] ad = new double[na];
					for (int j = 0; j < na; j++) {
						ntot += 1;
						if (bin[i][j]) {
							varHM.put("r", new Double(divrad[i][j]));
							varHM.put("d", new Double(2 * divrad[i][j]));
							varHM.put("p", new Double(divpl[i][j]));
							varHM.put("bo", new Double(divbo[i]));

							double val = eval.getValue(varHM);
							if (val < 0.) {
								E.warning("expression produced negative point density " + val +
										expression + " " + stringVariables(varHM));
								val = 0.;
							}


							ad[j] = val;
							if (val > 0.) {
								nnz += 1;
							}
							if (val > maxden) {
								maxden = val;
							}
						}
					}
					ret[i] = ad;
				}
				// E.info("expression " + expression + " applied on " + nin + " of " + ntot);
			} else {
				E.error("invalid expression " + eval.getErrorMessage());
			}
		}
		// E.info("made densities, max=" + maxden + " nnz=" + nnz);
		return ret;
	}


	public double[] getSingleItemRNChans(CompartmentTree ctree) {
		double[] ret = new double[ctree.size()];
		E.missing();
		
		return ret;
	}



	public double[] getRNChans(CompartmentTree ctree) {
		ctree.checkMetrics();
		double[] ret = new double[ctree.size()];

		double bd =  1.;

		HashMap<String, Double> varHM = new HashMap<String, Double>();
		ctree.getRootCompartment().exportVariables(varHM);



		if (expression == null || expression.trim().length() == 0) {
			int icpt = 0;
			for (Compartment cpt : ctree.getCompartments()) {
				ret[icpt] = bd * cpt.getArea();
				icpt += 1;
			}

		} else {
		Evaluator eval = new Evaluator(expression, varHM);

		if (eval.valid()) {
			int icpt = 0;
			for (Compartment cpt : ctree.getCompartments()) {
				cpt.exportVariables(varHM);
				double val = eval.getValue(varHM);
				if (val < 0.) {
					E.warning("expression produced negative point density " + val +
							expression + " " + stringVariables(varHM));
					val = 0.;
				}
				ret[icpt] = bd * val * cpt.getArea();
				icpt += 1;
			}

		} else {
			E.error("invalid expression " + eval.getErrorMessage());
		}
		}
		applyMasks(ctree, ret);
		 return ret;
	}



	private String stringVariables(HashMap<String, Double> vhm) {
		StringBuffer sb = new StringBuffer();
		for (String s : vhm.keySet()) {
			sb.append(s + "=" + vhm.get(s).doubleValue() + ",  ");
		}
		return sb.toString();
	}


	public String getExpression() {
		 return expression;
	}


	public Object getCachedColor() {
		return cachedColor;
	}

	public String getColor() {
		return color;
	}


	public void setDensityExpression(String expr) {
		 expression = expr;
	}



	public void setColor(String s) {
		color = s;
		cachedColor = null;
	}



	public void cacheColor(Object obj) {
		 cachedColor = obj;
	}


	public void flagChange() {
		changed = true;
	}

	public boolean hasChanged() {
		return changed;
	}

	public void unflagChanged() {
		changed = false;
	}




	public boolean hasConstraints() {
		boolean ret = false;
		if (constraints.size() > 0) {
			ret = true;
		}
		return ret;
	}


	public void reseed() {
		seed = (int)(1.e6 * Math.random());
	}


	public boolean dependsOnLabel(String lbl) {
		boolean ret = false;
		for (PopulationConstraint pc : constraints) {
			if (pc.dependsOn(lbl)) {
				ret = true;
			}
		}
		return ret;
	}


	public boolean isRelative() {
		return relDefined;
	}

	public void setNonRelative() {
		relDefined = false;
	}

	public void setRelativeDensity(double f, String stgt) {
		relDefined = true;
		relFactor = f;
		relTarget = stgt;
	}

	public double getRelFactor() {
		return relFactor;
	}

	public String getRelTarget() {
		return relTarget;
	}



	public DistribPopulation getRelTargetPopulation() {
		 return relTargetPop;
	}



	public Object getBestColor() {
		Object ret = null;
		if (cachedColor != null) {
			ret = cachedColor;
		} else {
			ret = color;
		}
		return ret;
	}



	public void setSingle() {
		fixTotal = true;
		totalNumber = 1;
	}



	public void setPointID(String sid) {
		targetPointID = sid;
	}



	public void setNumID(int idx) {
		numid = idx;
	}

	
	public int getNumID() {
		return numid;
	}
}
