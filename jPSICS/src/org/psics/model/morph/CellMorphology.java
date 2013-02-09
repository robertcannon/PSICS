package org.psics.model.morph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.Standalone;
import org.psics.geom.Geom;
import org.psics.geom.Position;
import org.psics.geom.Vector;
import org.psics.model.ModelElement;
import org.psics.morph.TreePoint;
import org.psics.num.math.MersenneTwister;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.phys.PhysicalCoordinate;
import org.psics.quantity.units.Units;

import org.psics.util.ArrayUtil;

@ModelType(standalone = true, usedWithin = {}, tag = "The geometrical structure of a cell", info = "This specifies the positions and sizes of "
		+ "the soma and processes of the cell to be modelled. It is designed for representing "
		+ "experimentally derived structures arising from some form of digitization process.")
public class CellMorphology extends ModelElement implements AddableTo, Standalone {

	@Container(tag = "points in the structure", contentTypes = { Point.class })
	public ArrayList<Point> c_points = new ArrayList<Point>();

	@Container(tag = "perpendicular branches", contentTypes = { Branch.class })
	public ArrayList<Branch> c_branches = new ArrayList<Branch>();

	@Flag(required = false, tag = "Treat the setion between adjacent points as square ended fustrums rather" +
			"than with round ends")
	public boolean squareCaps = false;
	
	private Point r_rootPoint;

	private boolean p_resolved = false;


	final static int TAPERED = 1;
	final static int UNIFORM = 2;
	int p_defaultSection = TAPERED;


	MersenneTwister mersenne;

	private ArrayList<Point> p_relPoints;

	private boolean p_shifted = false;

	public CellMorphology() {

	}


	public String getID() {
		return id;
	}


	public void setID(String sid) {
		id = sid;
	}


	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("Cell morphology with " + c_points.size() + " points");
		if (c_points.size() < 5) {
			sb.append(" (");
			for (Point rp : c_points) {
				sb.append(rp.toString());
			}
			sb.append(").");
		} else {
			sb.append(".");
		}
		return sb.toString();
	}


	public void add(Object obj) {
		if (obj instanceof Branch) {
			c_branches.add((Branch)obj);

		} else if (obj instanceof Point) {
			c_points.add((Point) obj);

		} else {
			E.error("cant add " + obj);
		}
	}


	public void preResolve() {
		HashMap<String, Point> pointHM = new HashMap<String, Point>();
		for (Point p : c_points) {
			pointHM.put(p.getID(), p);
		}
		for (Branch b : c_branches) {
			String sid = b.getID();
			if (sid != null) {
				pointHM.put(sid, b);
			}
		}


		for (Point p : c_points) {
			if (p.parent != null) {
				if (pointHM.containsKey(p.parent)) {
					p.p_parent = pointHM.get(p.parent);
				} else {
					E.error("point refers to non-existent parent: " + p.parent);
				}
			} else {
				p.p_parent = null;
				// E.info("got point with no parent id " + p);
			}
		}

		for (Branch b : c_branches) {
			if (b.parent != null) {
				if (pointHM.containsKey(b.parent)) {
					b.p_parent = pointHM.get(b.parent);
				} else {
					E.error("point refers to non-existent parent: " + b.parent);
				}
			} else {
				b.p_parent = null;
				// E.info("got point with no parent id " + p);
			}
		}
	}




	public void resolve() {
		preResolve();
		r_rootPoint = null;
		for (Point p : c_points) {
			p.deChild();
		}


		for (Point p : c_points) {
			Point ppar = p.getParent();
			if (ppar == null) {
				if (r_rootPoint == null) {
					r_rootPoint = p;
				} else {
					E.warning("can only have one point without a parent. We already have " + r_rootPoint
							+ " but now also have " + p);
				}


			} else {
				ppar.addChild(p);
			}
		}


		// remove duplicate points, as occur in a Neuron import where a new
		// segment with a different
		// radius starts with a point at the same position as the parent
		ArrayList<Point> togo = new ArrayList<Point>();
		for (Point p : c_points) {
			Point ppar = p.getParent();
			if (ppar != null && p.isAbsolute() && ppar.isAbsolute() &&
					Geom.distanceBetween(p.getPosition(), ppar.getPosition()) < 0.001) {
				if (p.onSurface) {
					// leave it - it will be moved later
			 		
				} else {
				togo.add(p);
				ppar.removeChild(p);
				for (Point pch : p.getChildren()) {
					pch.setParent(ppar);
					ppar.addChild(pch);
					// pch.minor = true;
				}
				}
			}
		}
		if (togo.size() > 0) {
			E.info("removing " + togo.size() + " duplicate points");
		}
		c_points.removeAll(togo);


		p_relPoints = new ArrayList<Point>();
		for (Point p : c_points) {
			if (p.isRelative()) {
				if (resolveRelative(p)) {
					// OK - dont need to put in list
				} else {
					p_relPoints.add(p);
				}
			}
		}


		resolveRelative();


		HashMap<Point,ArrayList<Double>>  facHM = new HashMap<Point, ArrayList<Double>>();
		HashMap<Point, ArrayList<Point>> jptHM = new HashMap<Point, ArrayList<Point>>();

		for (Branch b : c_branches) {
			Point pdist = b.getParent();
			Point pprox = pdist.getParent();

			Vector vmain = Geom.fromToVector(pprox.getPosition(), pdist.getPosition());
			double len = Geom.length(vmain);

			if (b.hasLength()) {
				double doff = 0.;
				if (b.hasOffset()) {
					doff = b.getOffset();

				} else if (b.hasPosition()) {
					Vector vbr = Geom.fromToVector(pprox.getPosition(), b.getPosition());
					doff = len - Geom.dotProduct(vbr, vmain) / len;

				} else {
					E.warning("a branch with a length needs either an offset or a position " + b);
					doff = 1;
				}

				double fb = (len - doff) / len;
				Position pwk = Geom.midPosition(pprox.getPosition(), pdist.getPosition(), fb);


				double blen = b.getLength();
				if (mersenne == null) {
					mersenne = new MersenneTwister(12345l); // TODO allow seed change
				}

				Vector va = Geom.vector(-1 + 2 * mersenne.random(),
										-1 + 2 * mersenne.random(),
										-1 + 2 * mersenne.random());
				Vector vperp = Geom.crossProduct(va, vmain);
				vperp.multiplyBy(blen / Geom.length(vperp));
				Position pbr = Geom.translatedPosition(pwk, vperp);
				b.setPosition(pbr);
			}


			Vector vbranch = Geom.fromToVector(pprox.getPosition(), b.getPosition());
			double cp = Geom.dotProduct(vbranch, vmain);
			double f = cp / (len * len);
			if (!facHM.containsKey(pdist)) {
				facHM.put(pdist, new ArrayList<Double>());
				jptHM.put(pdist, new ArrayList<Point>());
			}
			facHM.get(pdist).add(new Double(f));

			Position pjoin = Geom.midPosition(pprox.getPosition(), pdist.getPosition(), f);
			Point tpj = new Point(b.getID() + "-join",
					pjoin, f * pdist.getR() + (1. - f) * pprox.getR(), false);
			pdist.removeChild(b);
			b.setParent(tpj);
			tpj.addChild(b);
			jptHM.get(pdist).add(tpj);
		}

		resolveRelative();

		if (p_relPoints.size() > 0) {
			E.fatalError("failed to resolve some relative points " + p_relPoints.size() + " " +
					p_relPoints.get(0));
		}

		for (Point pdist : facHM.keySet()) {
			Point ppr = pdist.getParent();
			ArrayList<Point> pja = jptHM.get(pdist);
			int[] sort = ArrayUtil.getSortOrder(facHM.get(pdist));

			ppr.removeChild(pdist);
			for (int i = 0; i < sort.length; i++) {
				Point tpj = pja.get(sort[i]);
				ppr.addChild(tpj);
				tpj.setParent(ppr);

				tpj.y = new PhysicalCoordinate(tpj.y.getNativeValue(), Units.um);

				ppr = tpj;
			}
			ppr.addChild(pdist);
			pdist.setParent(ppr);
		}



		// any "partof" value that occurs only once should be a label;
		HashMap<String, Point> ocHM = new HashMap<String, Point>();
		HashSet<String> multHS = new HashSet<String>();
		for (Point p : c_points) {
			String pof = p.partof;
			if (pof != null && pof.length() > 0) {
				if (multHS.contains(pof)) {

				} else {
					if (ocHM.containsKey(pof)) {
						ocHM.put(pof, null);
						multHS.add(pof);

					} else {
						ocHM.put(pof, p);
					}
				}
			}
		}
		for (String s : ocHM.keySet()) {
			if (ocHM.get(s) != null) {
				ocHM.get(s).partToLabel();
			}
		}


		if (r_rootPoint == null) {
			E.error("cell appears to have no root point? there must be a loop...");
		}
		
		p_resolved = true;
	}


	class PointPair {

		Point point;
		TreePoint tPoint;


		private PointPair(Point p, TreePoint tp) {
			point = p;
			tPoint = tp;
		}
	}


	private boolean resolveRelative(Point p) {
		boolean done = false;
		Point pb = p.p_parent;
		if (pb != null && !pb.isRelative()) {
			Point pa = pb.p_parent;
			if (pa != null && !pa.isRelative()) {
				double len = Geom.distanceBetween(pa.getPosition(), pb.getPosition());
				Position pnew = Geom.midPosition(pa.getPosition(), pb.getPosition(), (len + p.beyond.getNativeValue()) / len);
				p.setPosition(pnew);
				done = true;
			}
		}
		return done;
	}


	private void resolveRelative() {
		while (true) {
			ArrayList<Point> togo = new ArrayList<Point>();
			for (Point p : p_relPoints) {
				if (resolveRelative(p)) {
					togo.add(p);
				}
			}
			if (togo.size() == 0) {
				break;
			} else {
				p_relPoints.removeAll(togo);
			}
		}
	}



	private TreePoint makeTreePoint(Point p) {
		TreePoint ret = new TreePoint(p.getID(), p.getPartOf(), p.getLabel(),
				p.getPosition(), p.getR(), p.isMinor());
		/*
		 * if (p.getLabel() != null && p.getLabel().length() > 2) { E.info("made
		 * tp with label " + p.getLabel()); }
		 */
		return ret;
	}


	public void printPoints() {
		for (Point p : c_points) {
			E.info("Point " + p.getID() + " " + p.getX() + " " + p.getY() + " " + p.getR() + " " + p.getParent());
		}
	}

	
	public boolean getSquareCaps() {
		return squareCaps;
	}

	public TreePoint[] exportTreePoints(boolean sqc) {
		 
		
		// printPoints();
		if (!p_resolved) {
			resolve();
		}
		if(!p_shifted) {
			checkConnected();
			shiftOnSurface(sqc);
			p_shifted = true;
		}
		TreePoint rootTP = makeTreePoint(r_rootPoint);
		ArrayList<TreePoint> tps = new ArrayList<TreePoint>();
		tps.add(rootTP);

		// queue rather than recursion here to avoid filling the heap;
		ConcurrentLinkedQueue<PointPair> clq = new ConcurrentLinkedQueue<PointPair>();
		clq.add(new PointPair(r_rootPoint, rootTP));

		while (!clq.isEmpty()) {
			PointPair wkp = clq.remove();

			for (Point chp : wkp.point.getChildren()) {
				TreePoint chTP = makeTreePoint(chp);
				// E.info("made new tp " + chTP);

				clq.add(new PointPair(chp, chTP));
				tps.add(chTP);

				TreePoint parentTP = wkp.tPoint;
				TreePoint.neighborize(parentTP, chTP);
			}
		}
		TreePoint[] atp = tps.toArray(new TreePoint[tps.size()]);
		nullWork(atp);
		recParentize(atp[0], 0);
		return atp;
	}


	private void nullWork(TreePoint[] pts) {
		for (TreePoint tp : pts) {
			tp.setWork(-1);
		}
	}


	// TODO - maybe call this in CellMorphology before export?
	private int recParentize(TreePoint tp, int idx) {
		int nadd = 0;
		tp.setWork(idx + nadd);
		nadd += 1;
		for (TreePoint nbr : tp.getNeighbors()) {
			if (nbr.getWork() < 0) {
				nbr.setParent(tp);
				nadd += recParentize(nbr, idx + nadd);
			}
		}
		return nadd;
	}


	public void setPoints(ArrayList<? extends Point> pa) {
		c_points.clear();
		c_points.addAll(pa);
		int nnp = 0;
		for (Point p : c_points) {
			if (p.hasParent()) {
				// OK;
			} else {
				nnp += 1;
			}
		}
		if (nnp > 1) {
			E.error("Multiple points have no parent " + nnp);
		}

	}


	public void checkConnected() {
		HashSet<Point> hs = new HashSet<Point>();
		hs.addAll(c_points);
		hs.remove(r_rootPoint);

		ConcurrentLinkedQueue<Point> clq = new ConcurrentLinkedQueue<Point>();
		clq.add(r_rootPoint);

		while (!clq.isEmpty()) {
			Point wkp = clq.remove();
			for (Point chp : wkp.getChildren()) {
				hs.remove(chp);
				clq.add(chp);
			}
		}

		if (hs.size() == 0) {
			// OK;
		} else {
			E.error("points that are noones child?");
			for (Point p : hs.toArray(new Point[hs.size()])) {
				E.info("orphan point " + p);
			}
		}

	}

	
	public void shiftOnSurface(boolean squareCaps) {
		// E.info("shifting to surface");
		
		for (Point p : c_points) {
			if (p.isOnSurface()) {
				E.info("moving onsurface pt " + p);
				shiftToSurface(p, squareCaps);			
			}
		}
	}
	
	
	public void shiftToSurface(Point p, boolean squareCaps) {
		Point pp = p.getParent();
		Vector shift = null;
		if (squareCaps) {
			shift = Geom.fromToVector(p.getPosition(), pp.getPosition());
		} else {
		   Vector v = Geom.unitX();
		   if (p.getChildren().size() > 0) {
			   Point c = p.getChildren().get(0);
			   v = Geom.fromToVector(p.getPosition(), c.getPosition());
			   v.multiplyBy(pp.getR() / Geom.length(v));
			   Position pdest = Geom.translatedPosition(pp.getPosition(), v);
			   shift = Geom.fromToVector(p.getPosition(), pdest);
		   }
		}
		shiftTree(p, shift);
	}
	
	public void shiftTree(Point p, Vector shift) {
		ConcurrentLinkedQueue<Point> clq = new ConcurrentLinkedQueue<Point>();
		clq.add(p);

		while (!clq.isEmpty()) {
			Point wkp = clq.remove();
			wkp.translate(shift);
			for (Point chp : wkp.getChildren()) {
				clq.add(chp);
			}
		}
		
		// TODO this is a bit of a fiddle so we don't get zero length segments. Could make the 
		// remeshing cope with points in same position with different radii.
		p.minor = true;
		if (p.getChildren().size() > 0) {
			Point c = p.getChildren().get(0);
			Position pnew = Geom.positionBetween(p.getPosition(), c.getPosition(), 0.01);
			p.setPosition(pnew);
		}	   
	}

	
	public void addPoint(String aid, double x, double y, double z, double r, String atyp, String pid) {
		Point p = new Point(aid, x, y, z, r);
		if (pid == null) {
			// E.info("added a point with a null parent");
		} else {
			p.setParentID(pid);
			if (defaultsUniform()) {
				p.setMinor();
			}
		}
		if (atyp != null && atyp.trim().length() > 0) {
			p.setPartOfCode(atyp);
		}
		c_points.add(p);
	}


	public void importTreePoints(TreePoint[] treePoints) {
		c_points.clear();
		for (TreePoint tp : treePoints) {
			Position pos = tp.getPosition();
			String parid = null;
			if (tp.parent != null) {
				parid = tp.parent.getID();
			}
			Point p = new Point(tp.getID(), pos.getX(), pos.getY(), pos.getZ(), tp.getRadius(), parid);


			if (tp.hasLabels()) {
				for (String s : tp.getLabels()) {
					p.addLabel(s);
					if (s.indexOf("lab") >= 0) {
						E.info("tp added label " + s);
					}
				}
			}
			c_points.add(p);
		}

	}


	public void setDefaultUniform() {
		p_defaultSection = UNIFORM;
	}

	public void setDefaultTapered() {
		p_defaultSection = TAPERED;
	}

	private boolean defaultsUniform() {
		return (p_defaultSection == UNIFORM);
	}
}
