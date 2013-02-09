package org.psics.geom;

public class GBall extends GPosition implements Ball {

	double radius;

	int iwork;
	double rwork;

	public GBall(Position p, double r) {
		super(p);
		radius = r;
		iwork = -1;
	}

	public GBall(double x, double y, double z, double r) {
		 super(x, y, z);
		 radius = r;
		 iwork = -1;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double r) {
		radius = r;
	}

	public Ball makeCopy() {
		Ball ret = new GBall(this, radius);
		ret.setWork(iwork);
		return ret;
	}

	public void setWork(int iw) {
		iwork = iw;
	}

	public int getWork() {
		return iwork;
	}

	public void setRWork(double rw) {
		rwork = rw;
	}

	public double getRWork() {
		return rwork;
	}
}
