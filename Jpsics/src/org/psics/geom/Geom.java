package org.psics.geom;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.util.SortUtil;


public final class Geom {


	static final double EPS = 1.e-7;




	public static void main(String[] argv) {
		GVector[] vectors = {new GVector(-3., 0.2, -0.7),
		new GVector(3., 2, 1),
		new GVector(4., -6, 0.1)};


		for (GVector v1 : vectors) {
			Rotation r = fromZRotation(v1);
			Vector uz = unitZ();
			Vector vres = r.getRotatedVector(uz);
			vres.multiplyBy(length(v1));
			System.out.println("v1=" + v1 + "  vres=" + vres);
		}
	}


   public static Position midpoint(Position p1, Position p2) {
      return new GPosition(0.5 * (p1.getX() + p2.getX()),
                           0.5 * (p1.getY() + p2.getY()),
                           0.5 * (p1.getZ() + p2.getZ()));
   }


	public static Position translatedPosition(Position p, Vector v) {
		return new GPosition(p.getX() + v.getDX(), p.getY() + v.getDY(), p.getZ() + v.getDZ());
	}


   public static Translation translation(Position p) {
      return new GTranslation(p);
   }


   public static Vector fromToVector(Position p1, Position p2) {
      return new GVector(p2.getX() - p1.getX(),
                         p2.getY() - p1.getY(),
                         p2.getZ() - p1.getZ());
   }

   public static Vector fromToVector(double[] p1c, double[] p2c) {
	      return new GVector(p2c[0] - p1c[0], p2c[1] - p1c[1], p2c[2] - p1c[2]);
   }



   public static Vector unitX() {
      return new GVector(1., 0., 0.);
   }

   public static Vector unitY() {
      return new GVector(0., 1., 0.);
   }

   public static Vector unitZ() {
      return new GVector(0., 0., 1.);
   }

   public static Vector xyProjection(Vector v) {
      return new GVector(v.getDX(), v.getDY(), 0.);
   }


  public static Line line(Position pa, Position pb) {
	  return new GLine(pa, pb);
  }


   public static Rotation fromZRotation(Vector v) {
      Vector v1 = unitZ();
      double phi = angleBetween(v1, v);

      GRotation gr1 = aboutYRotation(-phi);
      Vector vp = xyProjection(v);
      double theta = posAngleBetween(unitX(), vp);
   //   System.out.println(" " + theta);
      GRotation gr2 = aboutZRotation(theta);

      GRotation grot = gr2.times(gr1);
      return grot;
   }



   public static GRotation aboutZRotation(double angle) {
      GRotation gr = new GRotation(GRotation.Z_AXIS, angle);
      return gr;
   }

   public static GRotation aboutYRotation(double angle) {
      GRotation gr = new GRotation(GRotation.Y_AXIS, angle);
      return gr;
   }



   public static double length(Vector v) {
       double x = v.getDX();
       double y = v.getDY();
       double z = v.getDZ();
       double l2 = x*x + y*y + z*z;
       double l = Math.sqrt(l2);
       return l;
   }

   public static double dotProduct(Vector v1, Vector v2) {
      double d = 0.;
      d += v1.getDX() * v2.getDX();
      d += v1.getDY() * v2.getDY();
      d += v1.getDZ() * v2.getDZ();
      return d;
   }

   // result between 0 and PI
   public static double angleBetween(Vector v1, Vector v2) {
        double cosphi = dotProduct(v1, v2) / (length(v1) * length(v2));
        double phi = 0.;
        if (cosphi <= -1.) {
        	// rounding errors
        	phi = Math.PI;
        } else if (cosphi > 1.) {
        	phi = 0.;

        } else {
        	phi = Math.acos(cosphi);
        }
        if (phi < 0.01) {
     //   	E.info("small angle ?? " + v1 + " " + v2);

        }
        return phi;
   }


   //result between 0 and 2 PI, angle needed to rotate v1 onto v2 counterclockwise
   public static double posAngleBetween(Vector v1, Vector v2) {
	   double p1 = polarAngle(v1);
	   double p2 = polarAngle(v2);
	   double ret = p2 - p1;
	   if (ret < 0.) {
		   ret += 2 * Math.PI;
	   }
	   return ret;
   }


   public static double polarAngle(Vector v) {
	   double pa = Math.atan2(v.getDY(), v.getDX());
	   if (pa < 0.) {
		   pa += 2 * Math.PI;
	   }
	   return pa;
   }


   // the rotation angle that will move the z projection of v1 onto v2;
   public static double zRotationAngle(Vector v1, Vector v2) {
      double cx = v1.getDX() * v2.getDX() + v1.getDY() * v2.getDY();
      double cy = -1 * v1.getDY() * v2.getDX() + v1.getDX() * v2.getDY();
      double phi = Math.atan2(cy, cx);
      return phi;
   }


   public static Vector vector(double x, double y, double z) {
       return new GVector(x, y, z);
   }


   public static Position position(double x, double y, double z) {
      return new GPosition(x, y, z);
   }

   public static Position position(Position p) {
	      return new GPosition(p);
   }


   public static Vector getToVector(Position p) {
      return vector(p.getX(), p.getY(), p.getZ());
   }


   public static Position endPosition(Vector vr) {
       return position(vr.getDX(), vr.getDY(), vr.getDZ());
   }


   public static double xyDistanceBetween(Position pa, Position pb) {
       double dx = pa.getX() - pb.getX();
       double dy = pa.getY() - pb.getY();
       double dz = pa.getZ() - pb.getZ();
       double l2 = dx*dx + dy*dy + dz*dz;
       double l = Math.sqrt(l2);
       return l;

   }

   public static double distanceBetween(Position pa, Position pb) {
       return length(fromToVector(pa, pb));
   }


   public static double distanceBetween(double[] pa, double[] pb) {
      double dx = pb[0] - pa[0];
      double dy = pb[1] - pa[1];
      double dz = pb[2] - pa[2];
      double r2 = dx * dx + dy * dy + dz * dz;
      double r = Math.sqrt(r2);
      return r;
   }

   public static double xyDistanceBetween(double[] pa, double[] pb) {
	      double dx = pb[0] - pa[0];
	      double dy = pb[1] - pa[1];
	      double dz = 0.;
	      double r2 = dx * dx + dy * dy + dz * dz;
	      double r = Math.sqrt(r2);
	      return r;
	   }




   public static Position cog(Position[] perim) {
      int n = perim.length;
      GPosition pos = new GPosition();
      for (int i = 0; i < n; i++) {
         pos.add(perim[i]);
      }
      GPosition ret = new GPosition(pos.getX()/n, pos.getY()/n, pos.getZ()/n);
      return ret;
   }


   public static double getArea(Position[] perim) {
      Vector vec = getNormal(perim);
      double area = length(vec);
      return area;
   }




   public static Vector getNormal(Position[] perim) {
      GVector vec = new GVector();
      int n = perim.length;
      for (int i = 1; i < n-2; i++) {
         Vector pa = fromToVector(perim[0], perim[i]);
         Vector pb = fromToVector(perim[i], perim[i+1]);
         vec.add(crossProduct(pa, pb));
      }
      return vec;
   }


   public static Vector getUnitNormal(Position[] perim) {
      Vector va = getNormal(perim);
      double d = length(va);
      GVector ret = new GVector(va.getDX()/d, va.getDY()/d, va.getDZ()/d);
      return ret;
   }


   public static Vector crossProduct(Vector pa, Vector pb) {
       double ax = pa.getDX();
       double ay = pa.getDY();
       double az = pa.getDZ();

       double bx = pb.getDX();
       double by = pb.getDY();
       double bz = pb.getDZ();

       double rx = ay * bz - az * by;
       double ry = az * bx - ax * bz;
       double rz = ax * by - ay * bx;
       return new GVector(rx, ry, rz);
   }


public static Position partPosition(Vector v, double f) {
	 return new GPosition(f * v.getDX(), f * v.getDY(), f * v.getDZ());
}


	public static Ball ball(Position p, double r) {
		return new GBall(p, r);
	}

	public static Position midPosition(Position p1, Position p2) {
		GPosition ret = new GPosition(0.5 * (p1.getX() + p2.getX()),
									  0.5 * (p1.getY() + p2.getY()),
									  0.5 * (p1.getZ() + p2.getZ()));
		return ret;
	}

	public static Position midPosition(Position p1, Position p2, double f) {
		double wf = 1. - f;
		GPosition ret = new GPosition(f * p2.getX() + wf * p1.getX(),
									  f * p2.getY() + wf * p1.getY(),
									  f * p2.getZ() + wf * p1.getZ());
		return ret;
	}


	public static Position intersection(Line la, Line lb) {
		// typically, in 3D they won't intersect,
		// so we should go for the midpoint of the line joining the points of closest apprach...
		// for now assume they are xy only

		double x1 = la.getXA();
		double y1 = la.getYA();
		double x2 = la.getXB();
		double y2 = la.getYB();
		double x3 = lb.getXA();
		double y3 = lb.getYA();
		double x4 = lb.getXB();
		double y4 = lb.getYB();

		double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		double num = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);

		if (denom == 0.) {
			E.error("no intersection for lines? " + la + " " + lb);
		}
		double u = num / denom;

		Position ret = Geom.position(x1 + u * (x2 - x1), y1 + u * (y2 - y1), 0.);
		return ret;
	}



	public static boolean linesIntersect(Line la, Line lb) {
		double x1 = la.getXA();
		double y1 = la.getYA();
		double x2 = la.getXB();
		double y2 = la.getYB();
		double x3 = lb.getXA();
		double y3 = lb.getYA();
		double x4 = lb.getXB();
		double y4 = lb.getYB();

		double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
		double num = (x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3);
		double u = num / denom;
		boolean ret = false;
		if (u > 0. && u < 1.) {
			ret = true;
		}
		return ret;
	}




	public static Ball midBall(Ball b1, Ball b2) {
		GBall ret = new GBall(midPosition(b1, b2), 0.5 * (b1.getRadius() + b2.getRadius()));
		return ret;
	}


	public static Ball midBall(Ball b1, Ball b2, double f) {
		GBall ret = new GBall(midPosition(b1, b2, f), f * b2.getRadius() + (1. - f) * b1.getRadius());
		return ret;
	}



	public static Ball ball(double x, double y, double z, double r) {
		return new GBall(x, y, z, r);
	}



	public static double carrotArea(Ball b, Ball b0) {
		return Math.PI * (b.getRadius() + b0.getRadius()) * Geom.distanceBetween(b, b0);
	}


	// TODO check
	public static double carrotVolume(Ball ba, Ball bb) {
		 double ra = ba.getRadius();
		 double rb = bb.getRadius();
		 double dr = (rb - ra);
		 double ret = 0.;
		 if (dr / (ra + rb) < 1.e-4) {
			 ret = Math.PI * ra * rb * distanceBetween(ba, bb);
		 } else {
			 double rf =  distanceBetween(ba, bb) / (rb - ra);
			 ret =  rf * Math.PI / 3. * (rb * rb * rb - ra * ra * ra);
		 }
		 return ret;
	}


	public static double carrotResistance(Ball ba, Ball bb) {
		 // integral of dx / r^2
		 double ra = ba.getRadius();
		 double rb = bb.getRadius();
		 double dg = Geom.distanceBetween(ba, bb);
		 double ret = dg / (Math.PI * ra * rb);
		 return ret;
	}



/*
	public static Position[] carrotClumpBoundary(Ball bc, Ball[] lims) {
		Position[] ret = null;
		if (lims.length == 1) {
			ret = carrotBoundary(bc, lims[0]);

		} else {
			ret = multiCarrotBoundary(bc, lims);
		}
		return ret;
	}
	*/






	public static Position[] carrotBoundary(Ball b0, Ball b1) {
		double x0 = b0.getX();
		double y0 = b0.getY();
		double x1 = b1.getX();
		double y1 = b1.getY();
		double r0 = b0.getRadius();
		double r1 = b1.getRadius();

        double dl = Math.sqrt((y1 - y0) * (y1 - y0) + (x1 - x0) * (x1 - x0));

       double vnx0 = (-r0 * (y1 - y0) / dl);
       double vny0 = (r0  * (x1 - x0) / dl);

       double vnx1 = (-r1 * (y1 - y0) / dl);
       double vny1 = (r1 * (x1 - x0) / dl);

       Position[] ret = {Geom.position(x0 + vnx0, y0 + vny0, 0),
    		   			 Geom.position(x1 + vnx1, y1 + vny1, 0),
    		   			 Geom.position(x1 - vnx1, y1 - vny1, 0),
    		   			 Geom.position(x0 - vnx0, y0 - vny0, 0)};
       return ret;
	}


	public static Position positionBetween(Position pa, Position pb, double f) {
		   	 GPosition ret = new GPosition(pa);
		   	 Vector v = Geom.fromToVector(pa, pb);
		   	 ret.add(Geom.partPosition(v, f));
		   	 return ret;
     }


	private static Ball getProjection(Ball b0, Projector proj) {
		Ball b = null;
		if (proj == null) {
			b = b0;
		} else {
			b = proj.project(b0);
		}
		return b;
	}



	public static Position[] makeBallBoundary(Ball b0, Projector proj) {
		int np = 36;
		Ball b = getProjection(b0, proj);

		Position[] ret = new Position[np];
		double r = b.getRadius();
		for (int i = 0; i < np; i++) {
			double a = 2. * i * Math.PI / np;
			ret[i] = position(b.getX() + r * Math.cos(a), b.getY() + r * Math.sin(a), b.getZ());
		}
		return ret;
	}


	
	
	
	public static Position[] makeSectionBoundary(Ball[] section, Projector proj) {
		return outerBoundary(ballList(section, proj));
	}
		
	private static ArrayList<Ball> ballList(Ball[] section, Projector proj) {
		ArrayList<Ball> bb = new ArrayList<Ball>();

		Ball[] psection = new Ball[section.length];
		for (int i = 0; i < section.length; i++) {
			psection[i] = getProjection(section[i], proj);
		}
		for (Ball b : psection) {
			bb.add(b);
		}
		for (int i = psection.length-2; i >= 1; i--) {
			bb.add(psection[i]);
		}

		return bb;
	}

	
	public static Position[] makeSquareSectionBoundary(Ball[] section, Projector proj) {
		return squareOuterBoundary(ballList(section, proj));
	}
	

	public static Position[] starfishBoundary(Ball bc, Ball[][] arms, Projector proj) {
		return outerBoundary(starfishBalls(bc, arms, proj));
	}
	
	public static Position[] squareStarfishBoundary(Ball bc, Ball[][] arms, Projector proj) {
		return squareOuterBoundary(starfishBalls(bc, arms, proj));
	}
	
	
	public static ArrayList<Ball> starfishBalls(Ball bc, Ball[][] arms, Projector proj) {
		Ball pbc = getProjection(bc, proj);
		Ball[][] parms = new Ball[arms.length][];
		for (int i = 0; i < arms.length; i++) {
			parms[i] = new Ball[arms[i].length];
			for (int j = 0; j < arms[i].length; j++) {
				parms[i][j] = getProjection(arms[i][j], proj);
			}
		}


		double[] angles = new double[parms.length];
		for (int i = 0; i < parms.length; i++) {
			angles[i] = Geom.polarAngle(Geom.fromToVector(pbc, parms[i][0]));
		}

		Ball[][] wkarms = new Ball[parms.length][];
		int[] so = SortUtil.ascendingIndexes(angles);
		for (int i = 0; i < arms.length; i++) {
			wkarms[i] = parms[so[i]];
		}


		ArrayList<Ball> bb = new ArrayList<Ball>();

		{
		Ball[]  arm = wkarms[0];
		for (int k = arm.length - 2; k >= 0; k--) {
			bb.add(arm[k]);
		}
		}

		for (int ia = 1; ia < wkarms.length; ia++) {
			bb.add(pbc);

			Ball[] arm = wkarms[ia];
			for (int k = 0; k < arm.length; k++) {
				bb.add(arm[k]);
			}
			for (int k = arm.length - 2; k >= 0; k--) {
				bb.add(arm[k]);
			}
		}

		bb.add(pbc);
		{
			Ball[]  arm = wkarms[0];
			for (int k = 0; k < arm.length; k++) {
				bb.add(arm[k]);
			}
		}

		return bb;
	}

	
	
	
	
	  @SuppressWarnings("unused")
	private static void printSegment(ArrayList<Ball> ba) {
		StringBuffer sbx = new StringBuffer();
		StringBuffer sby = new StringBuffer();
		StringBuffer sbr = new StringBuffer();

		for (Ball b : ba) {
			sbx.append(String.format("%10.2f", b.getX()));
			sby.append(String.format("%10.2f", b.getY()));
			sbr.append(String.format("%10.2f", b.getRadius()));
		}
		E.info("boundary X " + sbx.toString());
		E.info("boundary Y " + sby.toString());
		E.info("boundary R " + sbr.toString());

	}


	public static Position[] outerBoundary(ArrayList<Ball> balls) {
		ArrayList<Position> alp = new ArrayList<Position>();
		int nb = balls.size();
		for (int ib = 0; ib < balls.size(); ib++) {
			Ball ba = balls.get((ib -1 + nb) % nb);
			Ball bb = balls.get(ib);
			Ball bc = balls.get((ib + 1) % nb);

			double xa = ba.getX();
			double ya = ba.getY();
			double ra = ba.getRadius();

			double xb = bb.getX();
			double yb = bb.getY();
			double rb = bb.getRadius();

			double xc = bc.getX();
			double yc = bc.getY();
			double rc = bc.getRadius();


			double dab = Geom.xyDistanceBetween(ba, bb);
			double dbc = Geom.xyDistanceBetween(bb, bc);

			Vector vba = Geom.vector(xa - xb, ya - yb, 0.); // zb - za);
			Vector vbc = Geom.vector(xc - xb, yc - yb, 0.);
			double ab = Geom.posAngleBetween(vba, vbc);


			if (dab < EPS || dbc < EPS) {

				E.warning("segment " + ib + " of " + balls.size() + " has zero length? " + ba + " " + bb + " " + bc);
			}

			double vabx =  - (yb - ya) / dab;
		    double vaby = (xb - xa) / dab;

		    double vbcx = - (yc - yb) / dbc;
		    double vbcy = (xc - xb) / dbc;


		    double dac = Geom.distanceBetween(ba, bc);

		    if (ba == bc || dac < EPS) {
		    	// doubling back on self - two points;
		    	alp.add(Geom.position(xb - rb * vabx, yb - rb * vaby, 0));
	    		alp.add(Geom.position(xb + rb * vabx, yb + rb * vaby, 0));

		    } else if (ab > Math.PI + 0.1) {

		    	alp.add(Geom.position(xb - rb * vabx, yb - rb * vaby, 0));
		    	double pa0 = Geom.polarAngle(vba) + 0.5 * Math.PI;

		    	int nip = (int)((ab - Math.PI) / 0.1);
		    	double dip = (ab - Math.PI) / nip;
		    	for (int k = 1; k <= nip; k++) {
		    		double a = pa0 + k * dip;
		    		alp.add(Geom.position(xb + rb * Math.cos(a), yb + rb * Math.sin(a), 0.));
		        }


		    } else if (ab > Math.PI - 0.1) {
				  // pretty much straight - just the midpoint needed;
				  alp.add(Geom.position(xb - rb * vabx, yb - rb * vaby, 0));

		    } else {
		    	// acute angle - just one point at the intersection;

			    Position pt1 = Geom.position(xb -rb * vabx, yb - rb * vaby, 0);
			    Position pt2 = Geom.position(xb - rb * vbcx, yb - rb * vbcy, 0);

			    Position p1 = Geom.position(xa - ra * vabx, ya - ra * vaby, 0);
			    Position p2 = Geom.position(xc  - rc * vbcx, yc - rc * vbcy, 0);


			    Line l1 = Geom.line(p1, pt1);
			    Line l2 = Geom.line(pt2, p2);

			    if (Geom.linesIntersect(l1, l2)) {
			    	alp.add(Geom.intersection(l1, l2));
			    } else {
			    	// don't ad interesection (would project to a spike)
			    	// just join existing points
			    }
		    }
		}

			Position[] ret = alp.toArray(new Position[alp.size()]);


		return ret;
	}

	

	public static Position[] squareOuterBoundary(ArrayList<Ball> balls) {
		ArrayList<Position> alp = new ArrayList<Position>();
		int nb = balls.size();
		for (int ib = 0; ib < balls.size(); ib++) {
			Ball ba = balls.get((ib -1 + nb) % nb);
			Ball bb = balls.get(ib);
		
			double xa = ba.getX();
			double ya = ba.getY();
			double ra = ba.getRadius();

			double xb = bb.getX();
			double yb = bb.getY();
			double rb = bb.getRadius();

			double dab = Geom.xyDistanceBetween(ba, bb);
	 
			double vabx =  - (yb - ya) / dab;
		    double vaby = (xb - xa) / dab;
		    
		    alp.add(Geom.position(xa - ra * vabx, ya - ra * vaby, 0));
		    alp.add(Geom.position(xb -rb * vabx, yb - rb * vaby, 0));
		    alp.add(Geom.position(bb));
		   
		}

		Position[] ret = alp.toArray(new Position[alp.size()]);

		return ret;
	}

	
	
	
	

	public static double sectionLength(Ball[] section) {
		double ret = 0.;
		for (int i = 1; i < section.length; i++) {
			ret += distanceBetween(section[i-1], section[i]);
		}
		return ret;
	}


	public static double sectionLength(Ball b, Ball[] section) {
		double ret = distanceBetween(b, section[0]) + sectionLength(section);
		return ret;
	}





	public static double[][] perpCirc(Ball b, Ball bp) {
		// circle around b where it intesects plane perpendicular to the line of centers with bp

		double cx = b.getX();
		double cy = b.getY();
		double cz = b.getZ();
		Vector vft = fromToVector(b, bp);
		Rotation rot = fromZRotation(vft);
		double r = b.getRadius();
		int n = 12;
		double[][] ret = new double[n][3];
		double dt = 2. * Math.PI / n;
		for (int i = 0; i < n; i++) {
			double theta = i * dt;
			double x = r * Math.cos(theta);
			double y = r * Math.sin(theta);
			Vector v = new GVector(x, y, 0);
			Vector vr = rot.getRotatedVector(v);
			ret[i][0] = cx + vr.getDX();
			ret[i][1] = cy + vr.getDY();
			ret[i][2] = cz + vr.getDZ();
		}
		return ret;
	}










}
