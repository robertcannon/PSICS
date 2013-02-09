package org.psics.model.imports.neuron;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.E;
import org.psics.be.Transitional;
import org.psics.geom.Geom;
import org.psics.model.morph.CellMorphology;
import org.psics.model.morph.MorphPoint;
import org.psics.model.morph.Point;

public class NRNCell implements Transitional {

	public String name;

	public SetOfSegments segments;

	public SetOfCables cables;


	HashMap<String, NRNPoint> srcptHM;
	HashMap<String, MorphPoint> ptHM;
	ArrayList<MorphPoint> points;




	public ArrayList<NRNSegment> getSegments() {
		return segments.getSegments();
	}


	public Object getFinal() {
		return getCellMorphology(name);
	}


	public CellMorphology getCellMorphology(String id) {
		// E.info("finalizing from " + this + " " + setOfPoints.size() + " nseg=" + cell.getSegments().size());

		boolean gotRoot = false;

		ptHM = new HashMap<String, MorphPoint>();
		points = new ArrayList<MorphPoint>();


		ArrayList<NRNSegment> segs = getSegments();
		for (NRNSegment seg : segs) {

			String pid = seg.getParentID();

			if (pid == null) {
				if (gotRoot) {
					E.error("multiple points with no parent?");
				}
				gotRoot = true;
				// only allowed once in cell - defines the root segment
				MorphPoint rpp = getOrMakePoint(seg.getProximal(), "rootpoint");
				MorphPoint rpc = getOrMakePoint(seg.getDistal(), seg.id);
				rpc.setParent(rpp);
				String sn = seg.getName();
				if (sn != null) {
					rpp.addLabel(sn);
					rpc.addLabel(sn);
				}


			} else {
				MorphPoint rpp = ptHM.get(pid);
				MorphPoint rpc = getOrMakePoint(seg.getDistal(), seg.getID());
				String sn = seg.getName();
				if (sn != null) {
					rpc.addLabel(sn);
				}

				if (seg.getProximal() != null) {
					// could be better to attach to rpp.parent, rather than rpp
					NRNPoint p = seg.getProximal();
					MorphPoint w = new MorphPoint(p.getID(), p.getX(), p.getY(), p.getZ(), p.getR());
					if (rpp.getParent() != null &&
							distanceBetween(w, rpp.getParent()) < distanceBetween(w, rpp)) {
					   rpp = (MorphPoint)rpp.getParent();
					   rpc.minor = true;
					}
				}
				rpc.setParent(rpp);
			}
		}

		CellMorphology cm = new CellMorphology();
		cm.id = id;
		cm.setPoints(points);
		cm.resolve();
	    E.info("returning Neuron import " + cm);

	    cm.checkConnected();

	    return cm;
	}



	private double distanceBetween(Point a, Point b) {
		 return Geom.distanceBetween(a.getPosition(), b.getPosition());
	}



	private MorphPoint getOrMakePoint(NRNPoint p, String id) {
		MorphPoint ret = null;
			if (ptHM.containsKey(id)) {
				ret = ptHM.get(id);
			} else {

			//	E.info("new point at " + sp.getID() + " " + sp.getX() + " " + sp.getY() + " " + sp.getR());
				ret = new MorphPoint(id, p.getX(), p.getY(), p.getZ(), p.getR());
				ptHM.put(id, ret);
				points.add(ret);
			}


			return ret;
		}

}
