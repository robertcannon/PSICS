package org.psics.morph;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.geom.Ball;
import org.psics.geom.Geom;
import org.psics.num.Compartment;
import org.psics.num.CompartmentConnection;
import org.psics.num.CompartmentTree;

public class Compartmentizer {

	TreePoint[] srcPoints;

	boolean squareCaps;


	public Compartmentizer(TreePoint[] tpts, boolean sc) {
		srcPoints = tpts;
		squareCaps = sc;
	}


	public CompartmentTree makeCompartmentTree() {
		// as yet, tpa are centerpoints of elements. Need to convert them into
		// volumes (three points, three radii
		// for normal ones) stretching half way to each neighbor;
		int nc = srcPoints.length;
		Compartment[] cpts = new Compartment[nc];

		// index the tree points so they know where they are in the array
		for (int i = 0; i < nc; i++) {
			srcPoints[i].setWork(i);
			srcPoints[i].setTreeSequenceNumber(i);
		}

		for (int i = 0; i < nc; i++) {
			Compartment cpt = makeCompartment(srcPoints[i]);
			cpts[i] = cpt;
		}

		for (int i = 0; i < nc; i++) {
			Compartment cpt = cpts[i];
			for (TreePoint tpn : srcPoints[i].getNeighbors()) {
				int inbr = tpn.getWork();
				// only do each pair once;
				if (inbr > i) {
					connectCompartments(srcPoints[i], tpn, cpt, cpts[inbr]);
				}
			}
		}

		CompartmentTree ctree = new CompartmentTree(cpts);
		return ctree;
	}


	private void connectCompartments(TreePoint tpa, TreePoint tpb, Compartment ca, Compartment cb) {
		double area = 0.;
		double res = 0.;
		// double len = 0.;
		TreePoint chld = null;

		Compartment parcpt = null;
		Compartment chcpt = null;
		if (tpa.getWork() < tpb.getWork()) {
			parcpt = ca;
			chcpt = cb;
			chld = tpb;
		} else {
			parcpt = cb;
			chcpt = ca;
			chld = tpa;
			E.warning("wrong order?");
		}
		area = chld.getToParentArea();
		res = chld.getToParentResistance();
		// len = chld.getToParentLength();

		double cfac = 1. / res;

		connectCompartments(parcpt, chcpt, cfac, area);
	}


	private void connectCompartments(Compartment ca, Compartment cb, double cfac, double area) {
		CompartmentConnection ccon = new CompartmentConnection(ca, cb, cfac, area);
		ca.addConnection(ccon);
		cb.addConnection(ccon);


		ca.incrementArea(0.5 * area);
		cb.incrementArea(0.5 * area);

		// NB if cb is a minorChild we could do it assymetrically, (which is
		// what currently
		// gets drawn) and put all the area on cb, but this gives systematic
		// errors (see rallpack2)
		// TODO - make drawing corespond to actual compartmentalization when
		// there are
		// minor branches

	}


	private Compartment makeCompartment(TreePoint tpc) {
		int myidx = tpc.getTreeSequenceNumber();

		Compartment cpt = new Compartment(squareCaps);
		cpt.setSourceTreeSequenceNumber(myidx);
		if (tpc.getID() != null) {
			cpt.setID(tpc.getID());
			// E.info("imported id " + tpc.getID());
		}
		if (tpc.hasLabels()) {
			for (String s : tpc.getLabels()) {
				cpt.addLabel(s);
			}
		}

		cpt.setPartOf(tpc.getPartOf());

		ArrayList<TreePoint> majors = new ArrayList<TreePoint>();
		 

		TreePoint minorParent = null;

		ArrayList<TreePoint> nbrs = tpc.getNeighbors();


		/*
		 * for (TreePoint tp : tpc.getNeighbors()) { int idx =
		 * tp.getTreeSequenceNumber(); if (tpc.minor && idx < myidx) {
		 * minorParent = tp;
		 *  } }
		 */
		/*
		 * if (tp.minor && tp.getTreeSequenceNumber() > myidx) { // excludes
		 * parent even if it is flagged minor; minors.add(tp);
		 *  } else { majors.add(tp); } }
		 */
		// 04 Jan 2008 - still want a starfish compartment from minor neighbors
		// since that is
		// what the compartment area calculation assumes
		// feb 29 the issue here is with minor points and rallpack 3. We don't
		// want
		// a special case for them in that case, so maybe not at all?
		
		if (squareCaps) {
		for (TreePoint tp : tpc.getNeighbors()) {
			majors.add(tp);
		}
		
		} else {
			for (TreePoint tp : tpc.getNeighbors()) { 
				int idx = tp.getTreeSequenceNumber(); 
				if (tpc.minor && idx < myidx) {
				//      minorParent = tp;
				}
			}
				for (TreePoint tp : tpc.getNeighbors()) {
					majors.add(tp);
				}
			
		
		}

		/*
		E.info("Compartmentizing " + tpc);
		for (TreePoint tp : tpc.getNeighbors()) {
			E.info("     nbr " + tp);
		}
		*/
		
		
		int nmajor = majors.size();
		Ball b = tpc.getBall();

		// b.getWork() holds the index of the point in the original structure
		// that
		// we came from, or -1 if the point is new



		if (nmajor == 0) {
		    E.info("created spherical cpt - no major neighbors");
			cpt.setSpherical(b);

		} else if (nmajor == 1) {
			TreePoint tp = nbrs.get(0);

			Ball bp = tp.getBall();
			if (tp == minorParent) {
				bp.setRadius(b.getRadius());
				ArrayList<Ball> hs = makeFullSegment(bp, tpc.getPreInner(), b);
				cpt.setTerminal(hs);

			} else if (tp.getWork() < tpc.getWork()) {
				// the normal case
				ArrayList<Ball> hs = makeSecondHalfSegment(bp, tpc.getPreInner(), b, tpc.minor);
				cpt.setTerminal(hs);


			} else {
				// must be the root point with just one neighbor
				ArrayList<Ball> hs = makeHalfSegment(b, tp.getPreInner(), bp, tp.minor);
				E.info("setting root " + hs);
				cpt.setRootSegment(hs);  
			}


		} else if (nmajor == 2) {
			TreePoint pa = nbrs.get(0);
			TreePoint pb = nbrs.get(1);
			if (pa.getWork() < pb.getWork()) {
				// a parent, b child of child, as we want;
			} else {
				TreePoint pw = pa;
				pa = pb;
				pb = pw;
			}


			boolean isRoot = false;
			if (pa.getWork() > tpc.getWork() && pb.getWork() > tpc.getWork()) {
				isRoot = true;
				// E.info("found root compartment " + b.getRadius());
			}

			ArrayList<Ball> hsxb = makeHalfSegment(b, pb.getPreInner(), pb.getBall(), pb.minor);

			hsxb.remove(0); // already there at the end of hsax;

			if (hsxb.size() >= 2) {
				double dist = Geom.distanceBetween(hsxb.get(0), hsxb.get(1));
				if (dist < 0.005) {
					E.error("half seg with zero length? " + dist);
					E.info("b, pb, preinner " + b + " " + pb.getBall() + " " + pb.getPreInner().size());
					E.info("hsxb 0, 1 " + hsxb.get(0) + " " + hsxb.get(1));
				}
			}

			ArrayList<Ball> hsax = null;

			if (isRoot) {
				ArrayList<Ball> wk = makeHalfSegment(b, pa.getPreInner(), pa.getBall(), pa.minor);
				// we have to reverse it here for the root since as yet it will be going
				// the oppposite way from hsxb
				hsax = new ArrayList<Ball>();
				for (Ball bw : wk) {
					hsax.add(0, bw);
				}

			} else {
				if (pa == minorParent) {
					Ball bp = pa.getBall();
					bp.setRadius(b.getRadius());

					hsax = makeFullSegment(bp, tpc.getPreInner(), b);
				} else {
					hsax = makeSecondHalfSegment(pa.getBall(), tpc.getPreInner(), b, tpc.minor);
				}


				if (tpc.fromStructure()) {
					// keep the center point too;
				} else {
					hsax.remove(hsax.size() - 1);
				}
			}

			ArrayList<Ball> fs = new ArrayList<Ball>();
			fs.addAll(hsax);
			fs.addAll(hsxb);

			if (Geom.distanceBetween(b, pb.getBall()) < 0.01) {
				E.error("pb and b at same position? " + b + " " + pb);
			}

			cpt.setSection(fs);


		} else if (nmajor >= 3) {
			ArrayList<ArrayList<Ball>> arms = new ArrayList<ArrayList<Ball>>();
			Ball[] prox = new Ball[2];
			for (TreePoint tp : nbrs) {
				Ball btp = tp.getBall();

				if (tp.getWork() < tpc.getWork()) {

					ArrayList<Ball> ab = null;
					if (tp == minorParent) {
						btp.setRadius(b.getRadius());
						ab = makeFullSegment(btp, tpc.getPreInner(), b);
						prox[0] = btp;
						prox[1] = ab.get(1);
					} else {
						ab = makeSecondHalfSegment(btp, tpc.getPreInner(), b, tpc.minor);
						prox[0] = ab.get(0);
						prox[1] = ab.get(1);
					}
					ab = reverse(ab);
					ab.remove(0);
					arms.add(ab);

					// E.info("second half segment reversed from parent goes to
					// " + ab.get(0));

				} else {
					ArrayList<Ball> ab = makeHalfSegment(b, tp.getPreInner(), btp, tp.minor);
					if (b.getWork() != ab.get(0).getWork()) {
						E.info("removing diff pt index ? " + b.getWork() + " " + ab.get(0).getWork());
					}
					ab.remove(0);
					arms.add(ab);
				}
			}
			/*
			if (prox[0] == null) {
				E.info("branch pt at center? " + tpc.getWork() + " " + tpc.getSourceIndex());
			}
			*/
			
			cpt.setBranchPoint(b, arms, prox);

		}
		return cpt;
	}


	private Ball[] getBalls(Ball b, ArrayList<TreePoint> innersin, Ball bend) {
		ArrayList<TreePoint> inners = innersin;
		if (inners == null) {
			inners = new ArrayList<TreePoint>();
		}


		int ni = inners.size();
		Ball[] ret = new Ball[2 + ni];
		ret[0] = b;
		for (int i = 0; i < ni; i++) {
			ret[1 + i] = inners.get(i).getBall();
		}
		ret[ni + 1] = bend;
		return ret;
	}


	private double[] getLengths(Ball[] bi) {
		double[] ret = new double[bi.length];
		ret[0] = 0.;
		for (int i = 1; i < bi.length; i++) {
			ret[i] = ret[i - 1] + Geom.distanceBetween(bi[i - 1], bi[i]);
		}
		return ret;
	}


	private ArrayList<Ball> makeFullSegment(Ball b, ArrayList<TreePoint> inners, Ball bend) {
		Ball[] bi = getBalls(b, inners, bend);
		// double[] lb = getLengths(bi);
		ArrayList<Ball> ret = new ArrayList<Ball>();
		for (Ball bwk : bi) {
			ret.add(bwk);
		}
		return ret;
	}


	private ArrayList<Ball> makeHalfSegment(Ball b, ArrayList<TreePoint> inners, Ball bend, boolean minor) {
		Ball[] bi = getBalls(b, inners, bend);
		double[] lb = getLengths(bi);

		double hlb = 0.5 * lb[lb.length - 1];
		Ball bwk = null;

		ArrayList<Ball> ret = new ArrayList<Ball>();
		int iwk = 0;
		while (lb[iwk] < hlb) {
			ret.add(bi[iwk]);
			bwk = bi[iwk];
			iwk += 1;
		}
		double f = (hlb - lb[iwk - 1]) / (lb[iwk] - lb[iwk - 1]);
		// if one of the inners falls right on the mid-point then we don't need
		// an extra point to end the segment

		Ball blast = Geom.midBall(bi[iwk - 1], bi[iwk], f);
		if (bwk != null && Geom.distanceBetween(bwk, blast) < 0.2 && ret.size() > 1) {
			ret.remove(bwk);
			blast.setWork(bwk.getWork());
			if (bwk.getRWork() < 0) {
				E.error("copying neg rwork");
			}
			blast.setRWork(bwk.getRWork());
		}

		ret.add(blast);
		if (minor) {
			boolean first = true;
			double r = bend.getRadius();
			for (Ball bret : ret) {
				if (!first) {
					bret.setRadius(r);
					bret.setRWork(r);
				}
				first = false;
			}
			
			Ball bs = ret.get(0);
			Ball bf = ret.get(1);
			double db = Geom.distanceBetween(bs, bf);
			if (db > bs.getRadius()) {
				Ball bborder = Geom.midBall(bs, bf, bs.getRadius() / db);
				bborder.setRadius(bf.getRadius());
				ret.add(1, bborder);
			//	E.info("added " + bborder + " between " + bs + " and " + bf);
			}
			
			
		}
		return ret;
	}


	private ArrayList<Ball> makeSecondHalfSegment(Ball b, ArrayList<TreePoint> inners, Ball bend, boolean minor) {
		Ball[] bi = getBalls(b, inners, bend);
		double[] lb = getLengths(bi);

		double hlb = 0.5 * lb[lb.length - 1];

		int iwk = 1;
		while (lb[iwk] < hlb) {
			iwk += 1;
		}
		ArrayList<Ball> ret = new ArrayList<Ball>();
		double f = (hlb - lb[iwk - 1]) / (lb[iwk] - lb[iwk - 1]);
		if (f < 0.999) {
			Ball bfirst = Geom.midBall(bi[iwk - 1], bi[iwk], f);
			ret.add(bfirst);
			double rw = f * bi[iwk].getRWork() + (1. - f) * bi[iwk - 1].getRWork();
			bfirst.setRWork(rw);
			if (rw < 0) {
				E.error("set negative rw" + iwk + " " + inners.size());
			}
		}
		for (int i = iwk; i < bi.length; i++) {
			ret.add(bi[i]);
			// if (bi[i].getWork() == 1) {
			// E.info("xxx added ball with inex " + 1);
			// }
		}
		if (minor) {
			double r = bend.getRadius();
			for (Ball bret : ret) {
				bret.setRadius(r);
			}
		}

		return ret;
	}


	private ArrayList<Ball> reverse(ArrayList<Ball> src) {
		ArrayList<Ball> ret = new ArrayList<Ball>();
		for (int i = src.size() - 1; i >= 0; i--) {
			ret.add(src.get(i));
		}
		return ret;
	}

}
