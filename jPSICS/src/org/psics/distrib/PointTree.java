package org.psics.distrib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.psics.be.E;
import org.psics.geom.Geom;
import org.psics.geom.Position;
import org.psics.morph.LocalDiscretizationData;
import org.psics.morph.TreePoint;
import org.psics.num.CalcUnits;
import org.psics.num.Compartment;
import org.psics.quantity.phys.BulkResistivity;
import org.psics.quantity.phys.Length;

public class PointTree {


	TreePoint[] points;

	TreePoint rootPoint;

	HashMap<String, Compartment> idHM;

	HashMap<String, TreePoint> pointIDHM;
	
	BulkResistivity resistivity;

	boolean doneMetrics = false;


	double[][] divsas;
	double[][][] divendpos;
	double[][] divendrad;
	double[][] divplength;
	int[] divbo;
	boolean[] isterms;

	public PointTree(TreePoint[] pts) {
		points = pts;
		for (int i = 0; i < pts.length; i++) {
			pts[i].setIndex(i);
		}
		parentize();
		evaluatePointMetrics();
	}

	private void parentize() {
		rootPoint = points[0];
		nullWork(points);
		recParentize(rootPoint, 0);
	}

	private void nullWork(TreePoint[] pts) {
		for (TreePoint tp : pts) {
			tp.setWork(-1);
		}
	}


	private int recParentize(TreePoint tp, int idx) {
		int nadd = 0;
		tp.setWork(idx + nadd);
		nadd += 1;
		for (TreePoint nbr : tp.getNeighbors()) {
			if (nbr.getWork() < 0) {
				nbr.setParent(tp);
				nadd += recParentize(nbr, idx + nadd);
			} else {
				// nothing to do? - parent already set
				//	tp.setParent(nbr);
			}
		}
		return nadd;
	}


	public void checkMetrics() {
		if (!doneMetrics) {
			evaluatePointMetrics();
		}
	}



	private void deFlag() {
		for (TreePoint tp : points) {
			tp.wkFlag = false;
		}
	}


	public void evaluatePointMetrics() {
		 doneMetrics = true;
		 deFlag();
	     Stack<TreePoint> stack = new Stack<TreePoint>();
	     rootPoint.wkFlag = true;
	     stack.push(rootPoint);

	     while (!stack.empty()) {
	    	 	TreePoint pt = stack.pop();
				if (pt == rootPoint) {
					setRootMetrics(pt);
				} else {
					setMetrics(pt);
				}

				for (TreePoint ctp : pt.getNeighbors()) {
					if (ctp.wkFlag) {
						// already done;
					} else {
						ctp.wkFlag = true;
						stack.push(ctp);
					}
				}
	     }
	}


	private void setRootMetrics(TreePoint pt) {
		pt.r = pt.getRadius();
		pt.d = 2 * pt.r;
		pt.p = 0.;
		pt.bo = 0;
		pt.pathLength = 0.;
	}


	private void setMetrics(TreePoint pt) {
		pt.r = pt.getRadius();
		pt.d = 2 * pt.r;
		pt.bo = pt.parent.bo;
		if (pt.parent.nChildren() > 1) {
			pt.bo += 1;
		}
		double dp = Geom.distanceBetween(pt.getPosition(), pt.parent.getPosition());
		pt.setParentCenterDistance(dp);
		pt.p = pt.parent.p + dp;
		pt.pathLength = pt.p;
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
	     TreePoint root = rootPoint; // getRootPoint();
	     Stack<TreePoint> stack = new Stack<TreePoint>();
	     root.wkFlag = true;
	     stack.push(root);

	     while (!stack.empty()) {
				TreePoint cpt = stack.pop();
				if (bm[cpt.getIndex()]) {
					// hit a labelled point - stop;
				} else {
					// must be prox to any labelled point or on a path with no lables at all
					ret[cpt.getIndex()] = false;
					for (TreePoint cn : cpt.getNeighbors()) {
						if (!cn.wkFlag) {
							// cn.wkCpt = cpt;
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
		Stack<TreePoint> stack = new Stack<TreePoint>();
		for (TreePoint c : points) {
			if (bm[c.getIndex()]) {
				stack.push(c);
				c.wkFlag = true;
			}
		}

		while (!stack.isEmpty()) {
			TreePoint c = stack.pop();
		    TreePoint cp = c.parent;
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
		for (TreePoint c : points) {
			if (c.p > d) {
				d = c.p;
			}
		}
		return CalcUnits.makeLength(d);
	}



	public ArrayList<TreePoint> getPointsAtPathDistance(Length l) {
		double d = CalcUnits.getLengthValue(l);
		// POSERR if d falls exactly on boundary? should we walk the tree? don't think it matters
		ArrayList<TreePoint> ret = new ArrayList<TreePoint>();
		for (TreePoint c : points) {
			if (c.parent != null && c.p >= d && c.parent.p < d) {
				if (c.p - d < 0.5 * c.parentDistance) {
					ret.add(c);
				} else {
					if (!ret.contains(c.parent)) {
						ret.add(c.parent);
					}
				}
			}

		}
		// could check for any parent child pairs in ret and eliminate one?
		return ret;
	}





/*
	public TreePoint getRootPoint() {
		 // if there is a spherical compartment, return that, otherwise the one with the maximum volume;

		 TreePoint ret = points[0];
		 if (ret.isSpherical()) {
			 // look no further;
		 } else {
			 for (int i = 1; i < points.length; i++) {
				 TreePoint cpt = points[i];
				 if (cpt.isSpherical()) {
					 ret = cpt;
					 break;
				 } else if (cpt.getRadius() > ret.getRadius()) {
					 ret = cpt;
				 }
			 }
		 }
		 return ret;
	}
*/


	public int size() {
		return points.length;
	}




	public void setIndices() {
		for (int i = 0; i < points.length; i++) {
			points[i].setIndex(i);
		}
	}



	public void subdivide(double divsize, boolean squareCaps) {
		evaluatePointMetrics();

		int np = points.length;
		divsas = new double[np][];
		divendpos = new double[np][][];
		divendrad = new double[np][];
		divplength = new double[np][];
		divbo = new int[np];
		isterms = new boolean[np];

		// ADHOC - root must be first
		divsas[0] = new double[0];
		divendpos[0] = new double[0][0];
		divendrad[0] = new double[0];
		divplength[0] = new double[0];
		divbo[0] = 0;
		isterms[0] = false;

		if (np == 0 || points[0].nMajorChildren() == 0) {
			TreePoint tp = points[0];
			isterms[0] = true;
			divendrad[0] = new double[1];
			divendpos[0] = new double[1][];
			divplength[0] = new double[1];
			divendrad[0][0] = tp.getRadius();
			Position p = tp.getPosition();
			double[] pv = {p.getX(), p.getY(), p.getZ()};
			divendpos[0][0] = pv;

		}

		for (int i = 1; i < np; i++) {
			TreePoint tp = points[i];
			TreePoint par = tp.parent;

			double fstart = 0.;
			double dpar = tp.parentDistance;
			if (tp.minor && !squareCaps) {
				dpar -= par.getRadius();
				fstart = par.getRadius() / tp.parentDistance;
			}

			int nd = (int)(Math.round(dpar / divsize));
			if (nd <= 0) {
				nd = 1;
			}
			divbo[i] = tp.bo;

			double dl = dpar / nd;

			double[] sas = new double[nd];
			double[][] pos = new double[nd+1][3];
			double[] rad = new double[nd+1];
			double[] pl = new double[nd+1];
			isterms[i] = false;

			Position pa = par.getPosition();
			Position pb = tp.getPosition();

			double ax = pa.getX();
			double ay = pa.getY();
			double az = pa.getZ();
			double ar = par.getRadius();

			double ap = par.p;

			double bx = pb.getX();
			double by = pb.getY();
			double bz = pb.getZ();
			double br = tp.getRadius();
			double bp = tp.p;


			if (tp.minor) {
				// the starting point is not the center of the parent, but
				// where the branch emerges from parents surface
				double wf = 1. - fstart;
				ax = wf * ax + fstart * bx;
				ay = wf * ay + fstart * by;
				az = wf * az + fstart * bz;
				ap = wf * ap + fstart * bp;
				ar = tp.getRadius();
			}

			pos[0][0] = ax;
			pos[0][1] = ay;
			pos[0][2] = az;
			rad[0] = ar;
			pl[0] = ap;

			for (int j = 0; j < nd; j++) {
				double f = (j + 1.) / nd;
			//	double fc = (j + 0.5) / nd;
				pos[j+1][0] = f * bx + (1. - f) * ax;
				pos[j+1][1] = f * by + (1. - f) * ay;
				pos[j+1][2] = f * bz + (1. - f) * az;

				rad[j+1] = f * br + (1. - f) * ar;

				// pl[j] = fc * bp + (1. - fc) * ap; // TODO make sure uses of pl know it is the bounds
				pl[j+1] = f * bp + (1. - f) * ap;

				sas[j] = 2. * Math.PI * dl * 0.5 * (rad[j] + rad[j+1]);

			}
			divsas[i] =  sas;
			divendpos[i] =  pos;
			divendrad[i] =  rad;
			divplength[i] = pl;

		if (tp.nMajorChildren() == 0) {
			isterms[i] = true;
		}
		}
	}


	public double[][] getSurfaceAreas() {
		 return divsas;
	}

	public double[][][] getDivisionEndPositions() {
		 return divendpos;
	}

	public double[][] getDivisionEndRadii() {
		 return divendrad;
	}

	public double getPathLength(int i) {
		return points[i].p;
	}


	public int[] getDivBranchOrder() {
		return divbo;
	}

	public double[][] getDivPathLength() {
		return divplength;
	}

	public boolean[] getIsTerminal() {
		return isterms;
	}


	public TreePoint[] getPoints() {
		return points;
	}

	public TreePoint getIthPoint(int i) {
		 return points[i];
	}

	public void applyLocalDiscretization(LocalDiscretizationData ldd) {
		HashMap<String, TreePoint> phm = getPointIDHM();
		String sfrom = ldd.getFrom();
		String sto = ldd.getTo();
		double esz = ldd.getEsize();
		
		if (sfrom != null) {
			if (phm.containsKey(sfrom)) {
				TreePoint tpstart = phm.get(sfrom);
				TreePoint tpend = null;
				if (sto != null) {
					if (phm.containsKey(sto)) {
						tpend = phm.get(sto);
					} else {
						E.error("no such id " + sto + " needed by local discretization");
					}
				}
				if (tpend != null) {
					while (true) {
						tpend.setEsize(esz);
						// E.info("set ezise on " + tpend);
						tpend = tpend.parent;
						if (tpend == null || tpend == tpstart) {
							// don't set esize on the start point itself, only on its child - 
							// setting esize on path from root to a given terminal should not have it  
							// apply to all children of root
							 
							break;
						}
					}
					
				} else {
					recSetEsize(tpstart, esz);	
				}
				
			} else {
				E.error("no such id " + sfrom + " needed by local discretization");
			}
		}
	}
	
	private void recSetEsize(TreePoint tpa, double esize) {
		deFlag();
		Stack<TreePoint> stack = new Stack<TreePoint>();
		stack.push(tpa);

		while (!stack.isEmpty()) {
			TreePoint c = stack.pop();
			if (!c.wkFlag) {
				c.setEsize(esize);
				c.wkFlag = true;
				for (TreePoint tp : c.children) {
					stack.push(tp);
				}
			}
		}
	}

	
	
	
	HashMap<String, TreePoint> getPointIDHM() {
		if (pointIDHM == null) {
			pointIDHM = new HashMap<String, TreePoint>();
			for (TreePoint p : points) {
				if (!pointIDHM.containsKey(p.getID())) {
					pointIDHM.put(p.getID(), p);
				}
				for (String s : p.getLabels()) {
					if (pointIDHM.containsKey(s)) {
						// leave the first visited label;
					} else {
						pointIDHM.put(s, p);
					}
				}
			}
		}
		return pointIDHM;
	}

}

