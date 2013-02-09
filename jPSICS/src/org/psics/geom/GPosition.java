package org.psics.geom;



public class GPosition implements Position, Movable {

   double x;
   double y;
   double z;

   public GPosition (double ax, double ay, double az) {
      x = ax;
      y = ay;
      z = az;
   }

   public GPosition(Position p) {
      this(p.getX(), p.getY(), p.getZ());
   }


   public String toString() {
      return ("(" + x + ", " + y + ", " + z + ")");
   }


   public GPosition() {
      this(0.,0., 0.);
   }

   public GPosition(Vector v) {
	 this(v.getDX(), v.getDY(), v.getDZ());
	 // TODO - OK or should we create a zero position and move it by the vector?
   }



   public double getX() {
      return x;
   }


   public double getY() {
      return y;
   }


   public double getZ() {
      return z;
   }

   public void moveTo(double ax, double ay, double az) {
      x = ax;
      y = ay;
      z = az;

   }

   public void add(Position position) {
      x += position.getX();
      y += position.getY();
      z += position.getZ();
    }

public void set(double x2, double y2, double z2) {
	 x = x2;
	 y = y2;
	 z = z2;
}

public void move(Vector v) {
	 x += v.getDX();
	 y += v.getDY();
	 z += v.getDZ();

}

}
