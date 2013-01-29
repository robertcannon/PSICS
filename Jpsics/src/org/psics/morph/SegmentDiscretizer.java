package org.psics.morph;

import java.util.ArrayList;

import org.psics.be.E;

public class SegmentDiscretizer {

	static final double EPS = 1.e-7;


	TreeSegment baseSegment;

	TreePoint[] basePoints;


	boolean minor = false;

	int nfit;
	TreePoint[] discPoints;


	public SegmentDiscretizer(TreeSegment seg) {
		baseSegment = seg;
		basePoints = baseSegment.getPointsArray();
	}



	// what about minor points - before we get here probably?
	// also maybe preserve labelled points before here, so some sections split into multiple segments
	// at named points - also before here



	public void discretize(double esize, boolean squareCaps, double interpPower) {
		int nbp = basePoints.length;
		TreePoint[] rnodes = new TreePoint[nbp];

		for (int i = 0; i < nbp; i++) {
			TreePoint p = basePoints[i];
			rnodes[i] = p.makeCopy();
			if (p.index < 0) {
				E.fatalError("lost index");
			}
			rnodes[i].setInStructure();
			if (rnodes[i].getSourceIndex() < 0) {
				E.error("lost source index....");
			}

			/*
			if (rnodes[i].getSourceIndex() == 5) {
				E.info("processing index 5 " + nbp);
				for (int k = 0; k < nbp; k++) {
					E.info("others " + basePoints[k].getSourceIndex() + " " + basePoints[k]);
				}
			}
			*/

		}

		if (basePoints[1].minor) {
			minor = true;
			rnodes[0].setRadius(rnodes[1].getRadius());
			if (squareCaps) {
				// E.info("got minor point, but using square Caps " + basePoints[1]);

			} else {
			    //TODO - anything? E.info("adjusting minor point for round caps " + basePoints[1]);
				double f = rnodes[0].getRadius() / (rnodes[0].distanceTo(rnodes[1]));
				if (f < 0.9) {
					rnodes[0].shiftTowards(rnodes[1], f);
				}
			}
		}



		TreePoint sna = rnodes[0];
		sna.setCumulativeArea(0.);
		sna.setCumulativeLength(0.);
		sna.setCumulativeResistance(0.);
		sna.setCumulativeRecR32(0.);


		for (int i = 1; i < nbp; i++) {
			TreePoint snb = rnodes[i];
			double dl = sna.distanceTo(snb);
			snb.setCumulativeLength(sna.getCumulativeLength() + dl);

			double ra = sna.getRadius();
			double rb = snb.getRadius();
			// POSSERR this is not the area of a fustrum, its just the bit parallel to the axis
			// if dl is zero, there's still an area, though we don't want to include it in the 
			// squareCaps case. In effect, this applies squareCaps and neglects the end area
			// when a thin section emerges from the end of a fat one.
			double darea = Math.PI * (rb + ra) * dl;
			snb.setCumulativeArea(sna.getCumulativeArea() +  darea);

			double dres = dl / (Math.PI * ra * rb);
			snb.setCumulativeResistance(sna.getCumulativeResistance() + dres);

			double dsrt = powRIntegral(ra, rb, dl, interpPower);

			snb.setCumulativeRecR32(sna.getCumulativeRecR32() + dsrt);
			sna = snb;
		}


		TreePoint snend = rnodes[nbp - 1];
		double tlb = snend.getCumulativeRecR32();


		//E.info("segment length is " + snend.getCumulativeLength() + " rec r32 " + tlb + " " +
		//		sna.getRadius() + " " + snend.getRadius());
		double esizeeff = esize / Math.pow(2., interpPower);
		// TODO this is so esize 1 with diameter 1 gives expected no of cpts - check....

		int ninner = (int)(tlb / esizeeff);
		double dlf = tlb / (ninner + 1);

		int nwn = ninner + 2;
		TreePoint[] wknodes = new TreePoint[nwn];


		int ipr = 0;
		for (int i = 0; i < nwn; i++) {
			double fcrr32 = i * dlf;

			ArrayList<TreePoint> preNodes = new ArrayList<TreePoint>();
			while (ipr < nbp-2 && fcrr32 > rnodes[ipr+1].getCumulativeRecR32()) {
				ipr += 1;
				preNodes.add(rnodes[ipr]);

			}
			TreePoint sa = rnodes[ipr];
			if (ipr + 1 >= rnodes.length) {
				E.error("interpolate miscount? " + fcrr32 + " " + rnodes[rnodes.length-1].getCumulativeRecR32());
			}

			TreePoint sb = rnodes[ipr+1];

			TreePoint newn = interpolateNode(sa, sb, fcrr32, interpPower);


			wknodes[i] = newn;
			newn.setDiscPre(preNodes);

			for (TreePoint sn : preNodes) {
				String sid = sn.getID();
				if (sid != null) {
					reapplyLabel(sn, sid, wknodes[i-1], newn);
				}
				if (sn.hasLabels()) {
					for (String s : sn.getLabels()) {
						reapplyLabel(sn, s, wknodes[i-1], newn);
					}
				}
			}
		}

		if (minor) {
			wknodes[1].setMinor();
		}

		for (int i = 1; i < nwn; i++) {
			wknodes[i].localizeParentConnection(wknodes[i-1]);
		}

		TreePoint oldlast = rnodes[nbp-1];
		TreePoint newlast = wknodes[nwn-1];

		double dlast = oldlast.distanceTo(newlast);
		if (Math.abs(dlast) > 1.e-6) {
			E.error("interpolation mess up..." + dlast);
		}
		 if (oldlast.getID() != null) {
			 newlast.setID(oldlast.getID());
		 }
		 newlast.setInStructure();

		 newlast.index = oldlast.index;
		 newlast.pathLength = oldlast.pathLength;

		baseSegment.setNewPoints(wknodes);
	}




	private void reapplyLabel(TreePoint sn, String sid, TreePoint lwn, TreePoint newn) {
		double dla = sn.getCumulativeLength() - lwn.getCumulativeLength();
		double dlb = newn.getCumulativeLength() - sn.getCumulativeLength();
		if (dla < dlb) {
			lwn.addLabel(sid);
			lwn.setPartOf(sn.getPartOf());
		} else {
			newn.addLabel(sid);
			newn.setPartOf(sn.getPartOf());
		}
	}



	private TreePoint interpolateNode(TreePoint sa, TreePoint sb, double fcrr32, double interpPower) {

		double a = sa.getCumulativeRecR32();
		double b = sb.getCumulativeRecR32();
		double frel = (fcrr32 - a) / (b - a);

		double ra = sa.getRadius();
		double rb = sb.getRadius();
		// flin is the position where the integral of 1/sqrt(r) from end a to flin is frel of the total
		double flin = powRInterp(ra, rb, sa.distanceTo(sb), frel, interpPower);

		if (flin > 1. + EPS || flin < 0. - EPS) {
			E.error("flin out of range " + flin);
		}

		TreePoint ret = new TreePoint();
		ret.locateBetween(sa, sb, flin);

		ret.setCumulativeArea(flin * sb.getCumulativeArea() + (1. - flin) * sa.getCumulativeArea());
		ret.setCumulativeResistance(flin * sb.getCumulativeResistance() + (1. - flin) * sa.getCumulativeResistance());

		return ret;
	}


/*
	private double recR32Integral(double ra, double rb, double dab) {
		double ret = 0.;

		if (Math.abs((rb - ra) / (ra + rb)) < 1.e-4) {
			ret = dab / (ra * Math.sqrt(ra));
		} else {
			ret = 2. * dab  / (rb - ra) * (1. / Math.sqrt(ra) - 1. / Math.sqrt(rb));
		}
		return ret;
	}



	private double recR32Interp(double ra, double rb, double dab, double f) {
		if (Math.abs((rb - ra)/(rb + ra)) < 1.e-4) {
			return f;
		}

		double drdx = (rb - ra) / dab; // dr/dx
		double xa = ra / drdx;

		// want r such that the integral comes to f of the total;

		double recrtr = f / Math.sqrt(rb) + (1. - f) / (Math.sqrt(ra));
		double r = 1. / (recrtr * recrtr);

		double x = r / drdx;
		double fx = (x - xa) / dab;
		return fx;
	}
*/

	private double powRIntegral(double ra, double rb, double dab, double p) {
		double ret = 0.;

		if (Math.abs((rb - ra) / (ra + rb)) < 1.e-4) {
			ret = dab * (Math.pow(ra, p));
		} else {
			double pp = p + 1;
			ret = dab  / ((rb - ra) * pp) * (Math.pow(rb, pp) - Math.pow(ra, pp));
		}
		return ret;
	}




	private double powRInterp(double ra, double rb, double dab, double f, double p) {
		if (Math.abs((rb - ra)/(rb + ra)) < 1.e-4) {
			return f;
		}

		double pp = p + 1;
		// want r such that the integral comes to f of the total;
		double rpp = f * Math.pow(rb, pp) + (1. - f)  * Math.pow(ra, pp);
		double r = Math.pow(rpp, 1. / pp);


		double drdx = (rb - ra) / dab;
		double xa = ra / drdx;


		double x = r / drdx;
		double fx = (x - xa) / dab;
		return fx;
	}

}
