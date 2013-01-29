package org.psics.num;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;

import org.psics.be.E;
import org.psics.geom.Projector;
import org.psics.quantity.phys.BulkResistivity;
import org.psics.quantity.phys.Length;
import org.psics.quantity.phys.SurfaceCapacitance;

public class CompartmentTree {


	Compartment[] compartments;

	HashMap<String, Compartment> idHM;


	BulkResistivity resistivity;

	boolean doneMetrics = false;


	boolean boundariesReady = false;

	Compartment[] srcDestMap;

	TreeMatcher treeMatcher;


	public CompartmentTree(Compartment[] cpts) {
		compartments = cpts;
	}



	public Compartment[] getCompartments() {
		return compartments;
	}


	public TreeMatcher getTreeMatcher() {
		if (treeMatcher == null) {
			treeMatcher = new TreeMatcher(this);
		}
		return treeMatcher;
	}


	private void deFlag() {
		for (Compartment cpt : compartments) {
			cpt.wkFlag = false;
		}
	}


	public void checkMetrics() {
		if (!doneMetrics) {
			evaluateMetrics();
		}
	}


	public void indexSource(int nsp) {
		setIndices();
		srcDestMap = new Compartment[nsp];
		for (Compartment cpt : compartments) {
			int[] isp = cpt.getSourcePoints();
			if (isp != null) {
				for (int i = 0; i < isp.length; i++) {
					if (isp[i] >= 0) {
						srcDestMap[isp[i]] = cpt;
					}
				}
			}
		}
		for (int i = 0; i < nsp; i++) {
			if (srcDestMap[i] == null) {
				E.info("src pt " + i + " no compartment for source point ");
			}
		}
	}



	public Compartment getCompartment(int i) {
		return compartments[i];
	}

	public Compartment getCompartmentForTreePoint(int itp) {
		return srcDestMap[itp];
	}


	public void evaluateMetrics() {
		 doneMetrics = true;
		 deFlag();
	     Compartment root = getRootCompartment();
	     Stack<Compartment> stack = new Stack<Compartment>();
	     root.wkFlag = true;
	     stack.push(root);

	     while (!stack.empty()) {
				Compartment cpt = stack.pop();
				if (cpt == root) {
					 cpt.setRootMetrics();
				} else {
					cpt.setMetrics(cpt.wkCpt);
				}

				for (Compartment cn : cpt.getNeighbors()) {
					if (cn.wkFlag) {
						// already done;
					} else {
						cn.wkCpt = cpt;
						cn.wkFlag = true;
						stack.push(cn);
					}
				}
	     }
	}


	public boolean[] getDistalPoints(boolean[] bm) {
		// we initially mark everything that isn't labelled as distal, then
		// walk through marking as non-distal until we hit a label. Only
		// the true distal parts dont get visited


		boolean[] ret = new boolean[bm.length];
		for (int i = 0; i < bm.length; i++) {
			ret[i] = true;
			if (bm[i]) {
				ret[i] = false;
			} else {
				ret[i] = true; // as yet, could be distal, not sure;
			}
		}
		 setIndices();
		 deFlag();
	     Compartment root = getRootCompartment();
	     Stack<Compartment> stack = new Stack<Compartment>();
	     root.wkFlag = true;
	     stack.push(root);

	     while (!stack.empty()) {
				Compartment cpt = stack.pop();
				if (bm[cpt.getIndex()]) {
					// hit a labelled point - stop;
				} else {
					// must be prox to any labelled point or on a path with no lables at all
					ret[cpt.getIndex()] = false;
					for (Compartment cn : cpt.getNeighbors()) {
						if (!cn.wkFlag) {
							cn.wkCpt = cpt;
							cn.wkFlag = true;
							stack.push(cn);
						}
					}
				}
	     }
		return ret;
	}





	public boolean[] getProximalPoints(boolean[] bm) {
		// stack up the marked segments and work in
		boolean[] ret = new boolean[bm.length];
		for (int i = 0; i < bm.length; i++) {
			ret[i] = false;
		}
		setIndices();
		deFlag();
		Stack<Compartment> stack = new Stack<Compartment>();
		for (Compartment c : compartments) {
			if (bm[c.getIndex()]) {
				stack.push(c);
				c.wkFlag = true;
			}
		}

		while (!stack.isEmpty()) {
			Compartment c = stack.pop();
		    Compartment cp = c.getParent();
			if (cp != null) {
				if (!cp.wkFlag) {
					ret[cp.getIndex()] = true;
					cp.wkFlag = true;
					stack.push(cp);
				}
			}
		}
		return ret;
	}





	public Length getMaximumPathDistance() {
		double d = 0.;
		for (Compartment c : compartments) {
			if (c.p > d) {
				d = c.p;
			}
		}
		return CalcUnits.makeLength(d);
	}



	public ArrayList<Compartment> getCompartmentsAtPathDistance(Length l) {
		double d = CalcUnits.getLengthValue(l);
		// POSERR if d falls exactly on boundary? should we walk the tree? don't think it matters
		ArrayList<Compartment> ret = new ArrayList<Compartment>();
		for (Compartment c : compartments) {
			if (Math.abs(c.p - d) <= 0.5 * c.length) {
				ret.add(c);
			}
		}
		// could check for any parent child pairs in ret and eliminate one?
		return ret;
	}



	public ArrayList<String> getChannelTypeIDs() {
		HashSet<String> hset = new HashSet<String>();
		for (Compartment cpt : compartments) {
			cpt.addChannelIDsIfNew(hset);
		}
		ArrayList<String> ret = new ArrayList<String>();
		ret.addAll(hset);
		return ret;
	}



	public Compartment getRootCompartment() {
		 // if there is a spherical compartment, return that, otherwise the one with the maximum volume;

		 Compartment ret = compartments[0];
		 if (ret.isSpherical()) {
			 // look no further;
		 } else {
			 for (int i = 1; i < compartments.length; i++) {
				 Compartment cpt = compartments[i];
				 if (cpt.isSpherical()) {
					 ret = cpt;
					 break;
				 } else if (cpt.volume > ret.volume) {
					 ret = cpt;
				 }
			 }
		 }
		 return ret;
	}



	public int size() {
		return compartments.length;
	}



	public String summarizeChannels() {
		 StringBuffer sb = new StringBuffer();
		 int maxnc = 0;
		 int minnc = -1;
		 int nctot = 0;
		 for (Compartment cpt : compartments) {
			 int nc = cpt.getNChannels();
			 if (nc > maxnc) {
				 maxnc = nc;
			 }
			 if (minnc < 0 || nc < minnc) {
				 minnc = nc;
			 }
			 nctot += nc;
		 }
		 sb.append("" + nctot + " channels, (" + minnc + " - " + maxnc + ")");
		 return sb.toString();
	}



	public String getMeshAsText() {
		 StringBuffer sb = new StringBuffer();
	      sb.append("volumeGrid " + compartments.length);
	      sb.append("\n");
	      for (int i = 0; i < compartments.length; i++) {
	         sb.append("" + compartments[i].getAsText());
	         sb.append("\n");
	      }
	      return sb.toString();
	}




	public void setMembraneCapacitance(SurfaceCapacitance membraneCapacitance) {
		double cmem = CalcUnits.getSpecificCapacitance(membraneCapacitance);

		for (Compartment cpt : compartments) {
			cpt.setMembraneCapacitance(cmem);
		}

	}


	public void printCompartmentInfo() {
		for (int i = 0; i < compartments.length; i++) {
			E.info("Cpt " + i + " " + compartments[i]);
		}
	}



	public void setBulkRestivitity(BulkResistivity br) {
		resistivity = br;

		double res = CalcUnits.getResistivityValue(br);
		for (Compartment cpt : compartments) {
			cpt.setResistivity(res);
		}
	}


	public Compartment getIthCompartment(int i) {
		return compartments[i];
	}



	public void toWork(int i) {
		for (Compartment cpt : compartments) {
			cpt.toWork(i);
		}
	}



	public void addChannelsFromWork(String ch1) {
		for (Compartment cpt : compartments) {
			cpt.setChannelsFromWork(ch1);
		}
	}




	public void setIndices() {
		for (int i = 0; i < compartments.length; i++) {
			compartments[i].setIndex(i);
		}
	}



	public void SmoothPositiveIntegerWork() {

		setIndices();

		double[] pp = new double[compartments.length];
		for (int i = 0; i < pp.length; i++) {
			pp[i] = compartments[i].getWork();
		}


	    // rearrange floating point work values for each compartment so that
		// they are all positive integers with the same total and as near the original
		// distribution as possible
		deFlag();
		{
		// first, denegativize, working back from terminals adding negative values to parent
		LinkedList<Compartment>  clq = new LinkedList<Compartment>();
		for (Compartment c : compartments) {
			if (c.isTerminal()) {
				clq.add(c);
			}
		}

		while (!clq.isEmpty()) {
			Compartment c = clq.remove();
			c.wkFlag = true;
			Compartment cp = c.getParent();
			if (cp != null) {
				if (c.getWork() < 0) {
					cp.incrementWork(c.getWork());
					c.setWork(0);
				}
				if (cp.wkFlag) {
				 //	E.oneLineWarning("miscount while smoothing (probably harmless)");

				} else {
					if (cp.isSegmentPoint()) {
						clq.addFirst(cp);

					} else if (cp.hasUnflaggedChild()) {
						// don't add it yet - the last incoming terminal will put it on the queue
					} else {
						clq.addLast(cp);
					}
				}
			} else {

				// warn if getWork is negative?
			}
		}
		}



		// Now integerize, again from terminals in
		deFlag();
		{
			LinkedList<Compartment>  clq = new LinkedList<Compartment>();
			for (Compartment c : compartments) {
				if (c.isTerminal()) {
					clq.add(c);
				}
			}

			while (!clq.isEmpty()) {
				Compartment c = clq.remove();
				c.wkFlag = true;
				 int nch = (int)c.getWork();
				 double rem = c.getWork() - nch;
				 c.setWork(nch);
				 Compartment cp = c.getParent();

				 if (cp != null) {
					cp.incrementWork(rem);
					if (cp.wkFlag) {
				//		E.error("already processed parent?");
				// (will have occured and been reported above)

					} else {
						if (cp.isSegmentPoint()) {
							clq.addFirst(cp);

						} else if (cp.hasUnflaggedChild()) {
							// don't add yet as above

						} else {
							clq.addLast(cp);
						}
					}
				} else {
					// E.info("got to cpt with no parent " + c.getIndex() + " rem=" + rem);
					// must be root poin
					if (rem > 0.5) {
						c.incrementWork(1.);
					}
				}
			}
		}

		double presum = 0.;
		double postsum = 0.;
		for (int i = 0; i < pp.length; i++) {
			presum += pp[i];
			double w = compartments[i].getWork();
			if (w < 0) {
				E.oneLineWarning("Channel balancing lead to a negative number of channels (" + w + ") on compartment " + i);
			}
//			E.info("pre post " + compartments[i].getIndex() + " " + pp[i] + " " + compartments[i].getWork());
			postsum += w;
//			E.info("pre post smoothing " + pp[i] + " " + compartments[i].getWork());
		}
		if (Math.abs(postsum - presum) > 0.5) {
			E.error("Integerization miscount: " + presum + " " + postsum);
		}
	}


	public boolean hasBoundaries() {
		return boundariesReady;
	}


	public void cacheProjectedBoundaries(Projector proj) {
		boundariesReady = false;
		 for (Compartment cpt : compartments) {
			 cpt.cacheProjectedBoundary(proj);
		 }
		 boundariesReady = true;
	}



	public int getNCompartments() {
		return compartments.length;
	}





	public String getRelativeCompartmentId(double lengthValue, String from, String towards) {
		checkMetrics();

		if (from == null && lengthValue < 0) {
			E.error("for negative lengtsh, the starting point must be specified with 'from'");
			return null;
		}

		TreeMatcher tm = getTreeMatcher();

		String ret = null;
		Compartment start = compartments[0];
		if (from != null) {
			Compartment cp = tm.getIdentifiedCompartment(from);
			if (cp != null) {
				start = cp;
			}
		}

		if (lengthValue < 0 || towards != null) {
			// easy case - find the towards point and walk back;
			double backdist = 0.;
			if (lengthValue < 0) {
				backdist = -lengthValue;
			} else if (towards != null) {
				Compartment tc = tm.getIdentifiedCompartment(towards);
				backdist = (tc.getPathLength() - (start.getPathLength() + lengthValue));
				start = tc;
				if (backdist < 0) {
					E.warning("cant go " + lengthValue + " towards " + towards);
					backdist = 0;
				}
			}

			double plo = start.getPathLength();
			Compartment wk = start;
			while (plo - wk.getPathLength() < backdist && wk.hasParent()) {
				wk = wk.getParent();
			}
			ret = getCompartmentID(wk);

		} else {
			double ptot = lengthValue + start.getPathLength();
			boolean[] bm = new boolean[compartments.length];
			for (int i = 0; i < bm.length; i++) {
				bm[i] = false;
			}
			bm[start.getIndex()] = true;
			boolean[] bd = getDistalPoints(bm);
			for (int i = 0; i < bm.length; i++) {
				if (bd[i] && compartments[i].getPathLength() > ptot) {
					// got to be cpt i or one of its ancestors;
					Compartment wk = compartments[i];
					double pl = wk.getPathLength();
					while (wk.hasParent()) {
						double pp = wk.getParent().getPathLength();
						if (ptot > 0.5 * (pl + pp)) {
							break;
						}
						pl = pp;
						wk = wk.getParent();
					}
					ret = getCompartmentID(wk);
					break;
				}
			}
		}
		if (ret == null) {
			E.error("cant locate relative cpt " + lengthValue + " from " + from + " towards " + towards);
		}
		return ret;
	}


	// bit fiddly = maybe just invalidate the matcher?
	private String getCompartmentID(Compartment cpt) {
		String ret = cpt.getID();
		if (ret == null) {
			ret = cpt.getNonNullID();
			if (treeMatcher != null) {
				treeMatcher.addIDd(cpt);
			}
		}
		return ret;
	}

}

