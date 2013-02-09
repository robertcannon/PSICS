package org.psics.geom;


public class Projector {

	private double m3xx = 1., m3yy = 1., m3zz = 1.;
	 private double m3xy = 0., m3xz = 0., m3yx = 0., m3yz = 0., m3zx = 0., m3zy = 0.;

	 private double w3cx = 0., w3cy = 0., w3cz = 0.;

	 private double w2cx = 0., w2cy = 0., w2cz = 0.;

	 public Projector() {

	 }

	public Projector(double[][] mat, double[] cen3, double[] cen2) {
		 m3xx = mat[0][0]; m3xy = mat[0][1]; m3xz = mat[0][2];
	      m3yx = mat[1][0]; m3yy = mat[1][1]; m3yz = mat[1][2];
	      m3zx = mat[2][0]; m3zy = mat[2][1]; m3zz = mat[2][2];

	      w3cx = cen3[0];
	      w3cy = cen3[1];
	      w3cz = cen3[2];

	      w2cx = cen2[0];
	      w2cy = cen2[1];
	      w2cz = cen2[2];
	}



	public final double xProj (double x, double y, double z) {
	      return  w2cx + m3xx * (x - w3cx) + m3xy * (y - w3cy) + m3xz * (z - w3cz);
	   }

	   public final double yProj (double x, double y, double z) {
	      return  w2cy + m3yx * (x - w3cx) + m3yy * (y - w3cy) + m3yz * (z - w3cz);
	   }

	   public final double zProj (double x, double y, double z) {
	      return  w2cz + m3zx * (x - w3cx) + m3zy * (y - w3cy) + m3zz * (z - w3cz);
	   }

	public Ball project(Ball b0) {
		double x = b0.getX();
		double y = b0.getY();
		double z = b0.getZ();
		Ball bret = new GBall(xProj(x, y, z), yProj(x, y, z), zProj(x, y, z), b0.getRadius());
		return bret;
	}

}
