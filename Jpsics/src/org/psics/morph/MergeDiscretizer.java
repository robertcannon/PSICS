package org.psics.morph;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.num.CompartmentTree;

/**
 * divide into segments either with fixed lengths or with equal integral of the
 * square root of the radiusr. Th sqrt(r) form balances the charging rate of
 * points in the final discretization and its definition is independent of the
 * electrical properties of the membrane. It provides a more consistent
 * discretization than the electrotonic length, and is valid in the absence of
 * persistent currents.
 *
 * Tapering segments are treated as such and not approximated by a series of
 * cylinders. The discretization respects the actual points of the structure to
 * which stimuli or recorders may be attached.
 */

public class MergeDiscretizer {

	TreePoint[] srcPoints;



	final static int FIXED = 1;
	final static int BALANCED = 2;

	int sdstyle;
	int maxnp;

	TreePoint[] outPoints;
	Resolution resolution;


	ArrayList<TreePoint> branchPoints;
	ArrayList<TreeSegment> segments;


	public MergeDiscretizer(TreePoint[] pts) {
		srcPoints = pts;
		for (int i = 0; i < srcPoints.length; i++) {
			srcPoints[i].setSourceIndex(i);
		}

		/*
		for (int i = 0; i < 20; i++) {
			E.info("src pt " + i + " " + srcPoints[i]);
		}
		*/
	}





	public void mergeDiscretize(double esize, boolean preservePoints,
							boolean squareCaps, double interpPower) {
		segmentize(preservePoints);

		if (segments.size() == 0) {
			outPoints = new TreePoint[1];
			outPoints[0] = srcPoints[0].makeCopy();

		} else {
			int nloc = 0;
			for (TreeSegment seg : segments) {
				SegmentDiscretizer sd = new SegmentDiscretizer(seg);
				double eswk = esize;
				if (seg.esize > 0) {
					eswk = seg.esize;
					nloc += 1;
				}
				
				sd.discretize(eswk, squareCaps, interpPower);
			}
			if (nloc > 0) {
				E.info("" + nloc + " segments used local element size");
			}
			
			
			outPoints = exportSegments();
		}
	}



	public CompartmentTree getCompartmentTree(double esize, int nmax, double interpPower) {
		return getCompartmentTree(esize, false, false, nmax, interpPower);
	}


	public CompartmentTree getCompartmentTree(double esize, boolean preservePoints,
				boolean squareCaps, int nmax, double interpPower) {
		CompartmentTree ret = null;
		mergeDiscretize(esize, preservePoints, squareCaps, interpPower);
		if (outPoints.length > nmax) {
			E.fatalError("Discretization yielded too many points " + outPoints.length + " " + nmax);
		} else {
			Compartmentizer cptz = new Compartmentizer(outPoints, squareCaps);
			ret = cptz.makeCompartmentTree();
		}
		return ret;
	}




	public void segmentize(boolean preservePoints) {
		branchPoints = new ArrayList<TreePoint>();
		segments = new ArrayList<TreeSegment>();


		TreePoint rootPoint = srcPoints[0];
		nullWork(srcPoints);
		recParentize(rootPoint, 0);
		if (preservePoints) {
			recTrivialSegmentize(rootPoint, null);
		} else {
			recSegmentize(rootPoint, null);
		}
	}



	private TreePoint[] exportSegments() {
		ArrayList<TreePoint> wtp = new ArrayList<TreePoint>();

		segments.get(0).recExportTo(wtp, null);

		return wtp.toArray(new TreePoint[wtp.size()]);
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



	private void recSegmentize(TreePoint tp, TreeSegment pseg) {

		TreeSegment rootSeg = null;
		 
		
		for (TreePoint ch : tp.getChildren()) {
			TreeSegment seg = new TreeSegment(tp);
			if (tp.esize > 0) {
				seg.setEsize(tp.esize);
			}
			
			if (pseg != null) {
				pseg.addChild(seg);

			} else if (rootSeg != null) {
				seg.setProximalParentConnection();
				rootSeg.addChild(seg);

			} else {
				rootSeg = seg;
			}

			while (ch.getChildCount() == 1) {
				seg.addInner(ch);
				if (ch.esize > 0) {
					seg.decreaseEsize(ch.esize);
				}
				ch = ch.getChild();
			}
			if (ch.esize > 0) {
				seg.decreaseEsize(ch.esize);
			}
			seg.setEnd(ch);
			segments.add(seg);
			if (ch.getChildCount() > 1) {
				recSegmentize(ch, seg);
			}
		}
	}


	private void recTrivialSegmentize(TreePoint tp, TreeSegment pseg) {
		// each point makes a new segment;
		TreeSegment rootSegment = null;
		for (TreePoint ch : tp.getChildren()) {
			TreeSegment seg = new TreeSegment(tp);
			if (pseg != null) {
				pseg.addChild(seg);
			} else if (rootSegment != null) {
				rootSegment.addChild(seg);
				seg.setProximalParentConnection();
			} else {
				rootSegment = seg;
			}

			seg.setEnd(ch);
			segments.add(seg);
			if (ch.getChildCount() > 0) {
				recTrivialSegmentize(ch, seg);
			}
		}
	}


}
