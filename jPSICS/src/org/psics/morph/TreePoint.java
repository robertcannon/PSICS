package org.psics.morph;


import org.psics.geom.Ball;
import org.psics.geom.GPosition;
import org.psics.geom.Geom;
import org.psics.geom.Position;
import org.psics.geom.Positioned;
import org.psics.geom.Vector;
import org.psics.be.E;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;


import java.util.HashMap;


public class TreePoint implements Positioned {

	String id;

	String partof;
	String firstLabel;
	HashSet<String> labels;

	Position position;
	double radius;


	public TreePoint[] nbr;
	public int nnbr;

	public TreePoint[] children;
	public TreePoint parent;


	public boolean minor = false;

	// temporary work variables
	public int iwork;
	public boolean dead; // marking prior to removal
	public int treeSequenceNumber;
	public boolean wkFlag;

	// x and y positions when part of a dendrogram, set by MLC
	public double dgx;
	public double dgy;


	private ArrayList<TreePoint> offsetChildren;

	private HashMap<TreePoint, String> segidHM;
	private HashMap<TreePoint, String> regionHM;

	public String name; // not sure about this POSERR;


	double crecr32;
	double clength;
	double carea;
	double cresist;


	double toParentArea;
	double toParentResistance;
	double toParentLength;

	boolean inStructure = false;

	ArrayList<TreePoint> preNodes;

	public int index = -1;

	// metrics for allocationg channels;
	public double r;
	public double d;
	public double p;
	public int bo;
	public double parentDistance;

	public double pathLength = -1;

	int sourceIndex = -1;

	// local discretization parameter
	double esize = -1.;
	

	public TreePoint() {
		nbr = new TreePoint[6];
		nnbr = 0;
		dead = false;

	}


	public TreePoint(String sid, String pof, String lbl, Position p, double r, boolean mn) {
		this();
		id = sid;
		position = Geom.position(p);
		radius = r;
		minor = mn;

		partof = pof;

		if (lbl != null) {
			firstLabel = lbl;
			labels = new HashSet<String>();
			labels.add(lbl);
		}
	}


	public void setID(String s) {
		id = s;
	}


	public void setIndex(int i) {
		index = i;
	}


	public int getIndex() {
		return index;
	}


	public void setEsize(double d) {
		esize = d;
	}

	// for SWC compatibility : just one label per point
	public void addLabel(String s) {
		if (s != null) {
			if (labels == null) {
				labels = new HashSet<String>();
				firstLabel = s;
			}
			labels.add(s);
		}
	}

	public void setLabel(String s) {
		if (labels != null) {
			labels = new HashSet<String>();
		}
		addLabel(s);
	}


	public String getID() {
		return id;
	}


	public void setPartOf(String s) {
		partof = s;
	}

	public String getPartOf() {
		return partof;
	}

	public void setMinor() {
		minor = true;
	}

	public void setNotMinor() {
		minor = false;
	}


	public boolean getMinor() {
		return minor;
	}


	public double getRadius() {
		return radius;
	}


	public void addLabels(Collection<String> slbs) {
		if (slbs != null) {
		if (labels == null) {
			labels = new HashSet<String>();
			if (slbs.size() > 0) {
				firstLabel = slbs.iterator().next(); // ADHOC - any elemen
			}
		}

			labels.addAll(slbs);
		}
	}


	public TreePoint makeCopy() {
		TreePoint tpr = new TreePoint(id, partof, null, position, radius, minor);
		if (firstLabel != null) {
			tpr.addLabel(firstLabel);
			if (labels.size() > 1) {
				for (String s : labels) {
					if (!s.equals(firstLabel)) {
						tpr.addLabel(s);
					}
				}
			}
		}
		tpr.inStructure = inStructure;
		tpr.toParentArea = toParentArea;
		tpr.toParentResistance = toParentResistance;
		tpr.toParentLength = toParentLength;
		tpr.preNodes = preNodes;
		tpr.pathLength = pathLength;
		tpr.index = index;
		tpr.sourceIndex = sourceIndex;

		return tpr;
	}


	public void setWork(int iw) {
		iwork = iw;
	}


	public int getWork() {
		return iwork;
	}


	public String toString() {
		return ("xyzr: " + position + "  " + radius + " nnbr=" + nnbr + (minor ? " (minor)" : ""));
	}


	public void setParent(TreePoint tp) {
		parent = tp;
		boolean got = false;
		for (int i = 0; i < nnbr; i++) {
			if (nbr[i] == parent) {
				got = true;
			}
		}
		if (!got) {
			E.error("setting a parent that isnt a neighbor?? " + parent);
		}

		syncChildren();
	}


	public int nChildren() {
		if (children == null) {
			syncChildren();
		}
		return children.length;
	}

	private void syncChildren() {
		if (parent == null) {
			children = new TreePoint[nnbr];
		} else {
			children = new TreePoint[nnbr - 1];
		}
		int ich = 0;
		for (int i = 0; i < nnbr; i++) {
			TreePoint atp = nbr[i];
			if (atp == parent) {
				// skip it;
			} else {
				children[ich] = atp;
				ich += 1;
			}
		}
	}


	public TreePoint[] getChildren() {
		if (children == null) {
			syncChildren();
		}
		return children;
	}


	public int getChildCount() {
		return (getChildren()).length;
	}


	private void clearChildren() {
		// called if neighbors change;
		children = null;
	}


	public TreePoint getChild() {
		TreePoint ret = null;
		if (children == null) {
			syncChildren();
		}
		if (children.length == 1) {
			ret = children[0];
		} else {
			E.error("get child called but have " + children.length + " children ");
		}
		return ret;
	}


	public void deParent() {
		parent = null;
		clearChildren();
	}


	public Position getPosition() {
		return position;
	}


	public void locateBetween(TreePoint cpa, TreePoint cpb, double f) {
		position = Geom.positionBetween(cpa.getPosition(), cpb.getPosition(), f);
		radius = (1. - f) * cpa.getRadius() + f * cpb.getRadius();
		pathLength = cpa.pathLength + f * Geom.distanceBetween(cpa.getPosition(), cpb.getPosition());
		if (f < 0.5) {
			addLabels(cpa.getLabels());

		} else if (f >= 0.5) {
			addLabels(cpb.getLabels());
		}

	}


	// REFAC - these should all be private, so only the
	// static methods that presever symmetry are visible
	public void addNeighbor(TreePoint cpn) {
		boolean has = false;
		for (int i = 0; i < nnbr; i++) {
			if (nbr[i] == cpn) {
				E.error("adding a neighbor we already have ");
				has = true;
			}
		}
		if (has) {
			// do nothing more - shouldn't have been called though;

		} else {
			if (nnbr >= nbr.length) {
				TreePoint[] pn = new TreePoint[2 * nnbr];
				for (int i = 0; i < nnbr; i++) {
					pn[i] = nbr[i];
				}
				nbr = pn;
			}
			nbr[nnbr++] = cpn;
		}
		clearChildren();
	}


	public void removeNeighbor(TreePoint cp) {
		int ii = -1;
		for (int i = 0; i < nnbr; i++) {
			if (nbr[i] == cp) {
				ii = i;
			}
		}
		if (ii >= 0) {
			for (int i = ii; i < nnbr - 1; i++) {
				nbr[i] = nbr[i + 1];
			}
			nnbr--;
		}
		if (cp == parent) {
			deParent();
		} else {
			clearChildren();
		}
	}


	public void replaceNeighbor(TreePoint cp, TreePoint cr) {

		int ii = -1;
		for (int i = 0; i < nnbr; i++) {
			if (nbr[i] == cp) {
				ii = i;
			}
		}
		if (ii >= 0) {
			nbr[ii] = cr;
		} else {
			E.error(" (replaceNeighbor) couldnt find nbr " + cp + " in nbrs list of " + this);
		}

		if (segidHM != null && segidHM.containsKey(cp)) {
			segidHM.put(cr, segidHM.get(cp));
		}
		if (regionHM != null && regionHM.containsKey(cp)) {
			regionHM.put(cr, regionHM.get(cp));
		}
		if (cp == parent) {
			setParent(cr);
		} else {
			clearChildren();
		}
	}


	public boolean hasNeighbor(TreePoint cp) {
		boolean hn = false;
		for (int i = 0; i < nnbr; i++) {
			if (nbr[i] == cp) {
				hn = true;
			}
		}
		return hn;
	}


	public void removeDeadNeighbors() {
		for (int i = nnbr - 1; i >= 0; i--) {
			if (nbr[i].dead) {
				removeNeighbor(nbr[i]);

			}
		}
	}


	// these are branches that start some way down a segment, but are
	// linked from here temporarily until the tree is discretized and a new
	// point
	// is available to have them connected from as neighbors
	public void addOffsetChild(TreePoint pch) {
		if (offsetChildren == null) {
			offsetChildren = new ArrayList<TreePoint>();
		}
		offsetChildren.add(pch);
	}


	public boolean hasOffsetChildren() {
		return (offsetChildren != null);
	}


	public ArrayList<TreePoint> getOffsetChildren() {
		return offsetChildren;
	}


	public double distanceTo(TreePoint cp) {
		return Geom.distanceBetween(position, cp.getPosition());
	}


	// POSERR no z
	public void movePerp(TreePoint ca, TreePoint cb, double dperp) {
		double dx = cb.getPosition().getX() - ca.getPosition().getX();
		double dy = cb.getPosition().getY() - ca.getPosition().getY();
		double f = Math.sqrt(dx * dx + dy * dy);
		dx /= f;
		dy /= f;


		position = Geom.position(position.getX() + dperp * dy, position.getY() - dperp * dx, 0.);
	}


	public static void neighborize(TreePoint tp, TreePoint tpn) {
		tp.addNeighbor(tpn);
		tpn.addNeighbor(tp);

	}


	public ArrayList<TreePoint> getNeighbors() {
		ArrayList<TreePoint> ret = new ArrayList<TreePoint>();
		for (int i = 0; i < nnbr; i++) {
			ret.add(nbr[i]);
		}
		return ret;
	}


	public boolean isEndPoint() {
		boolean ret = false;
		if (nnbr == 1) {
			ret = true;
		}
		return ret;
	}


	public TreePoint oppositeNeighbor(TreePoint tpp) {
		TreePoint ret = null;
		if (nnbr == 2) {
			if (nbr[0] == tpp) {
				ret = nbr[1];
			} else {
				ret = nbr[0];
			}
		}
		return ret;
	}


	public void setIDWith(TreePoint point, String s) {
		if (segidHM == null) {
			segidHM = new HashMap<TreePoint, String>();
		}
		// E.info("tp set region id to " + point + " as " + s);
		segidHM.put(point, s);
	}


	public void setRegionWith(TreePoint point, String s) {
		if (regionHM == null) {
			regionHM = new HashMap<TreePoint, String>();
		}
		regionHM.put(point, s);
	}


	public String regionClassWith(TreePoint tp) {
		String ret = null;
		if (regionHM != null && regionHM.containsKey(tp)) {
			ret = regionHM.get(tp);
		}
		return ret;
	}


	public String segmentIDWith(TreePoint tp) {
		String ret = null;
		if (segidHM != null && segidHM.containsKey(tp)) {
			ret = segidHM.get(tp);
		}
		return ret;
	}


	public int getNeighborCount() {
		return nnbr;
	}


	public void disconnect() {
		nnbr = 0;
		nbr = new TreePoint[0];
		deParent();
	}


	public TreePoint[] newPointArray(int nl) {
		return new TreePoint[nl];
	}


	public Ball getBall() {
		Ball ret = Geom.ball(position, radius);
		ret.setWork(index);
		ret.setRWork(pathLength);
    	return ret;
	}


	public void shiftTowards(TreePoint node, double f) {
		Vector v = Geom.fromToVector(this.getPosition(), node.getPosition());
		v.multiplyBy(f);
		GPosition gp = new GPosition(this.getPosition());
		gp.move(v);
		position = gp;
	}


	public void setCumulativeRecR32(double v) {
		crecr32 = v;
	}


	public void setCumulativeLength(double v) {
		clength = v;
	}


	public void setCumulativeArea(double v) {
		carea = v;
	}


	public void setCumulativeResistance(double v) {
		cresist = v;
	}


	public double getCumulativeLength() {
		return clength;
	}


	public double getCumulativeArea() {
		return carea;
	}


	public double getCumulativeResistance() {
		return cresist;
	}


	public double getCumulativeRecR32() {
		return crecr32;
	}


	public void setRadius(double d) {
		radius = d;

	}


	public void setDiscPre(ArrayList<TreePoint> apn) {
		preNodes = apn;
		if (apn.size() > 0 && apn.get(0).index < 0) {
			E.error("prenode has neg index");
		}
	}


	public void localizeParentConnection(TreePoint pn) {
		toParentArea = getCumulativeArea() - pn.getCumulativeArea();
		toParentResistance = getCumulativeResistance() - pn.getCumulativeResistance();
		toParentLength = getCumulativeLength() - pn.getCumulativeLength();
	}


	public double getToParentArea() {
		return toParentArea;
	}


	public double getToParentLength() {
		return toParentLength;
	}


	public double getToParentResistance() {
		return toParentResistance;
	}


	public void setInStructure() {
		inStructure = true;
	}


	public boolean fromStructure() {
		return (inStructure);
	}





	public boolean hasPreInner() {
		return (preNodes != null && preNodes.size() > 0);
	}


	public boolean hasPreInner(int n) {
		boolean ret = false;
		if (preNodes != null) {
			for (TreePoint tp : preNodes) {
				if (tp.getSourceIndex() == n) {
					ret = true;
				}
			}
		}
		return ret;
	}

	public void printPreInner() {
		StringBuffer sb = new StringBuffer();
		if (preNodes != null) {
			for (TreePoint tp : preNodes) {
				sb.append(" " + tp.getSourceIndex() + "    ");
			}
		}
		E.info("prenodes " + sb.toString());
	}


	public ArrayList<TreePoint> getPreInner() {
		return preNodes;
	}


	public void setTreeSequenceNumber(int i) {
		treeSequenceNumber = i;

	}


	public int getTreeSequenceNumber() {
		return treeSequenceNumber;

	}


	public boolean hasLabels() {
		return (labels != null && labels.size() > 0);
	}

	public HashSet<String> getLabels() {
		return labels;
	}

	public String getFirstLabel() {
		return firstLabel;
	}


	public int nMajorChildren() {
		int nmc = 0;
		for (int i = 0; i < nnbr; i++) {
			if (nbr[i] == parent) {
				// not a major child
			} else if (nbr[i].minor) {
				// not either
			} else {
				nmc += 1;
			}
		}
		return nmc;
	}


	public void setParentCenterDistance(double dp) {
		parentDistance = dp;
		// even for minors, have distance to parent _center_ here
	}


	public void setSourceIndex(int i) {
		 sourceIndex = i;
	}

	public int getSourceIndex() {
		return sourceIndex;
	}


	public void removeLabels(HashSet<String> tg) {
		 labels.removeAll(tg);
	}




/*
	public String getLabel() {
		return firstLabel;
	}
*/

}
