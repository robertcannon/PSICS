package org.psics.model.neuroml;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.Meta;
import org.psics.be.MetaContainer;
import org.psics.be.MetaItem;
import org.psics.be.Transitional;
import org.psics.geom.Geom;
import org.psics.model.electrical.ChannelPopulation;
import org.psics.model.electrical.DistributionRule;
import org.psics.model.morph.CellMorphology;
import org.psics.model.morph.MorphPoint;
import org.psics.model.morph.Point;

public class MorphMLCell implements MetaContainer, AddableTo, Transitional {

	public String name;

	public ArrayList<MorphMLSegment> segments = new ArrayList<MorphMLSegment>();

	public ArrayList<MorphMLCable> cables = new ArrayList<MorphMLCable>();

	public ArrayList<MorphMLCableGroup> cableGroups = new ArrayList<MorphMLCableGroup>();


	public String notes;

	HashMap<String, MorphMLPoint> srcptHM;
	HashMap<String, MorphPoint> ptHM;
	ArrayList<MorphPoint> points;

	public NeuroMLBiophysics biophysics;

	Meta meta;


	public void add(Object obj) {
		if (obj instanceof MorphMLCable) {
			cables.add((MorphMLCable)obj);
		} else if (obj instanceof MorphMLCableGroup) {
			cableGroups.add((MorphMLCableGroup)obj);

		} else if (obj instanceof NeuroMLMechanism) {
			// WTF just junk it

		} else {
			E.error("cant add " + obj);
		}
	}


	public void addMetaItem(MetaItem mi) {
		if (meta == null) {
			meta = new Meta();
		}
		meta.add(mi);
	}


	public ArrayList<MorphMLSegment> getSegments() {
		return segments;
	}


	public Object getFinal() {
		return getCellMorphology();
	}

	public CellMorphology getCellMorphology() {
		return getCellMorphology(name);
	}

	public CellMorphology getCellMorphology(String id) {
		// E.info("finalizing from " + this + " " + setOfPoints.size() + " nseg=" + cell.getSegments().size());

		boolean gotRoot = false;

		ptHM = new HashMap<String, MorphPoint>();
		points = new ArrayList<MorphPoint>();


		ArrayList<MorphMLSegment> segs = getSegments();

		for (MorphMLSegment seg : segs) {

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
				rpc.minor = true;
				String sn = seg.getName();
				if (sn != null) {
					rpp.addLabel(sn);
					rpc.addLabel(sn);
				}


			} else {
				MorphPoint rpp = ptHM.get(pid);
				MorphPoint rpc = getOrMakePoint(seg.getDistal(), seg.getID());
				rpc.minor = true;
				String sn = seg.getName();
				if (sn != null) {
					rpc.addLabel(sn);
				}

				if (seg.getProximal() != null) {
					// could be better to attach to rpp.parent, rather than rpp
					MorphMLPoint p = seg.getProximal();
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
	 //    E.info("returning MorphML import " + cm);

	    cm.checkConnected();

	    return cm;
	}



	public ArrayList<DistributionRule> getDistributionRules() {
		ArrayList<DistributionRule> dral = new ArrayList<DistributionRule>();
		// HashMap<String, DistributionRule> drHM = new HashMap<String, DistributionRule>();

		HashMap<String, String> cableSegHM = new HashMap<String, String>();
		for (MorphMLCable cbl : cables) {
			cableSegHM.put(cbl.getID(), cbl.getName());
		}

		for (MorphMLCableGroup gp : cableGroups) {
			DistributionRule dr = new DistributionRule(gp.getName());
			for (MorphMLCable cbl : gp.cables) {
				String sid = cableSegHM.get(cbl.getID());
				dr.addIncludeRegionMask("*" + sid + "*");
			}
			dral.add(dr);
		}
		return dral;
	}



	public ArrayList<ChannelPopulation> getChannelPopulations() {
		ArrayList<ChannelPopulation> ret = new ArrayList<ChannelPopulation>();
		if (biophysics != null) {
			for (NeuroMLMechanism mech : biophysics.getMechanisms()) {
				if (mech.getType().equals("Channel Mechanism")) {
					ChannelPopulation cp = new ChannelPopulation();
					cp.setID(mech.getName());

					ArrayList<NeuroMLParameter> params = mech.getParameters();
					if (params.size() == 1) {
						NeuroMLParameter p = params.get(0);
						String nm = p.getName();
						double d = p.getValue();
						String gp = p.getGroup();
						if (nm.equals("gmax")) {
							cp.setDensityExpression("" + d);
							cp.setDistribution(gp);
							ret.add(cp);

						} else {
							E.error("cant handle " + nm);
						}

					} else {
						E.error("cant handle param array of size " + params.size());
					}

				}
			}

		}

		return ret;
	}



	public ArrayList<NeuroMLProp> getProperties() {
		ArrayList<NeuroMLProp> ret = new ArrayList<NeuroMLProp>();
		if (biophysics != null) {
			 ret.addAll(biophysics.getProperties());
		}
		return ret;
	}



	private double distanceBetween(Point a, Point b) {
		 return Geom.distanceBetween(a.getPosition(), b.getPosition());
	}



	private MorphPoint getOrMakePoint(MorphMLPoint p, String id) {
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
