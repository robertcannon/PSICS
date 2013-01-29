package org.psics.morph;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.geom.Geom;

public class TreeSegment {

	TreePoint start;
	TreePoint end;
	ArrayList<TreePoint> innerPoints;

	TreePoint[] newPoints;
	TreePoint newEnd;

	boolean connectsToProximalPointOnParent = false;


	public ArrayList<TreeSegment> children;

	double esize = -1;
	

	public TreeSegment(TreePoint tp) {
		start = tp;
		children = new ArrayList<TreeSegment>();
	}


/*
	public TreeSegment(TreePoint tpa, TreePoint tpb, ArrayList<TreePoint> ipts) {
		this(tpa);
		end = tpb;
		innerPoints = ipts;

	}
*/

	public void addInner(TreePoint tp) {
		if (innerPoints == null) {
			innerPoints = new ArrayList<TreePoint>();
		}
		innerPoints.add(tp);
	}


	public void setEnd(TreePoint tp) {
		end = tp;
	}


	public void addChild(TreeSegment ts) {
		children.add(ts);
	}




	public void setNewPoints(TreePoint[] wknodes) {
		 newPoints = wknodes;

	}


	public void recExportTo(ArrayList<TreePoint> wtp, TreePoint parent) {
 		TreePoint prev = parent;
		if (parent == null) {
			prev = start.makeCopy();
			wtp.add(prev);
		}

		TreePoint startPeer = prev;

		for (int i = 1; i < newPoints.length; i++) {
			TreePoint tp = newPoints[i];
			TreePoint tpc = tp.makeCopy();

			if (Geom.distanceBetween(prev.getPosition(), tpc.getPosition()) < 0.001) {
				E.error("same point twice? " + prev + " " + tpc + " parent was " + parent);
			}

			TreePoint.neighborize(prev, tpc);

			wtp.add(tpc);
			prev = tpc;
		}


		for (TreeSegment ts : children) {
			if (ts.connectsToProximalPointOnParent) {
				ts.recExportTo(wtp, startPeer);
			} else {
				ts.recExportTo(wtp, prev);
			}
		}
	}



	public TreePoint[] getPointsArray() {
		TreePoint[] ret = null;
		if (innerPoints != null && innerPoints.size() > 0) {
			int nin = innerPoints.size();
			ret = new TreePoint[2 + nin];
			ret[0] = start;
			ret[ret.length-1] = end;
			for (int i = 0; i < nin; i++) {
				ret[1 + i] = innerPoints.get(i);
			}
		} else {
			ret = new TreePoint[2];
			ret[0] = start;
			ret[1] = end;
		}
		return ret;
	}



	public void setProximalParentConnection() {
		 connectsToProximalPointOnParent = true;
	}


	public void setEsize(double d) {
		esize = d;
	}

	public void decreaseEsize(double d) {
		if (esize < 0) {
			esize = d;
		} else if (d < esize) {
			esize = d;
		}
	}





}
