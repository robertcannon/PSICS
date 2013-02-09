package org.psics.num.math;


public class Interpolator {

	double[] xpts;
	double[] ypts;
	int np;
	double last;

	int ipt = 0;

	public Interpolator(double[] x, double[] y) {
		xpts = x;
		ypts = y;
		np = xpts.length;
		last = xpts[np-1];
	}


	public double valueAt(double x) {
		double ret = 0.;
		if (x <= xpts[0]) {
			ret = ypts[0];

		} else if (x >= last) {
			ret = ypts[np - 1];

		} else {
			if (x < xpts[ipt]) {
				while (true) {
					if (ipt == 0 || x < xpts[ipt-1]) {
						break;
					} else {
						ipt -= 1;
					}
				}

			} else {
				while (true) {
					if (ipt+1 == np-1 || x < xpts[ipt+1]) {
						break;
					} else {
						ipt += 1;
					}
				}
			}

			double f = (x - xpts[ipt]) / (xpts[ipt+1] - xpts[ipt]);

			ret = f * ypts[ipt+1] + (1. - f) * ypts[ipt];
		}

		return ret;
	}


}
