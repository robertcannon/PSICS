package org.psics.morph;

import org.psics.be.E;


// keeping these for now - not currently used anywhere - should throw out if no use arises.


public class Segmath {


	@SuppressWarnings("unused")
	private double rootRIntegral(double ra, double rb, double dab) {
		double ret = 0.;

		if (Math.abs((rb - ra) / (ra + rb)) < 1.e-4) {
			ret = dab * Math.sqrt(ra);
		} else {
			ret = (2. / 3.) * dab / (rb - ra) * (Math.pow(rb, 3. / 2.) - Math.pow(ra, 3. / 2.));
		}
     	return ret;
	}


	@SuppressWarnings("unused")
	private double xrootRInterp(double ra, double rb, double dab, double f) {
		if (Math.abs((rb - ra)/(rb + ra)) < 1.e-4) {
			return f;
		}

		// double delf = f * rootRIntegral(ra, rb, dab);
		double drdx = (rb - ra) / dab; // dr/dx
		double xa = ra / drdx;
		//  double xb = rb / ffa;

		// xa and xb are the end positions measured from where
		// the carrot comes to a point.

		// the integral of sqrt(r) dx is   2/3  / drdx  * (rb^3/2 - ra^3/2)
		// so need dx such that this is delf (= total_int / nseg)
		double r32 = f * Math.pow(rb, 3./2.)  + (1. - f) * Math.pow(ra, 3./2.);
		double r = Math.pow(r32, 2./3.);
		double x = r / drdx;
		double fx = (x - xa) / dab;
		return fx;


	}


	// following calculates the integrals and discretization in one go for a carrot shaped
	// segment. Not currently used.
	@SuppressWarnings("unused")
	private double[] getBalancedSubdivision(TreePoint cpa, TreePoint cpb, double del) {
 		double dab = cpa.distanceTo(cpb);
		double ra = cpa.getRadius();
		double rb = cpb.getRadius();

		double localDelta = del; // resolution.getLocalDelta(cpa, cpb);

		double fdist = 0.0;
		// fdist is to be the integral in question between pta and ptb;
		if (rb != ra) {
			fdist = ((2. / 3.) * dab / (rb - ra) * (Math.pow(rb, 3. / 2.) - Math
					.pow(ra, 3. / 2.)));
			// lbya = dab * (1./rb - 1./ra) / (Math.PI * (ra - rb));

		} else {
			fdist = dab * Math.sqrt(ra);
			// lbya = dab / (Math.PI * ra * ra);
		}
		// aseg = dab * Math.PI * (ra + rb);

		int nadd = (int) (fdist / localDelta);

		double[] dpos = new double[nadd];

		if (nadd > 0) {

			if (Math.abs((ra - rb) / (ra + rb)) < 0.01) {
				for (int i = 0; i < nadd; i++) {
					dpos[i] = (1. + i) / (nadd + 1.);
				}
			} else {
				// chop up the carrot;
				double delf = fdist / (nadd + 1);
				double ffa = (rb - ra) / dab; // dr/dx
				double xa = ra / ffa;
				double xb = rb / ffa;
				// xa and xb are the end positions measured from where
				// the carrot comes to a point.
				double x = xa;

				// the integral of sqrt(r) dx is
				// 2/3 * dx / (rb-ra) * (rb^3/2 - ra^3/2)
				// so need dx such that this is delf (= total_int / nseg)

				for (int i = 0; i < nadd + 1; i++) {
					double ttt = (delf * ffa * 3. / 2. + Math.pow(ffa * x,
							3. / 2.));
					double dx = Math.pow(ttt, (2. / 3.)) / ffa - x;
					x += dx;
					if (i < nadd) {
						dpos[i] = (x - xa) / dab;
					}
				}
				if (Math.abs(xb - x) > 1.e-5) {
					E.error("segment division " + xa + " " + xb + " " + x + " "
							+ nadd + " " + dab + " " + ra + " " + rb);
				}
			}
		}
		return dpos;
	}




}
