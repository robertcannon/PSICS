package org.psics.geom;

public class GLine implements Line {

	Position pa;
	Position pb;
	
	public GLine(Position p1, Position p2) {
		pa = p1;
		pb = p2;
	}
	
	public Position getPositionA() {
		return pa;
	}
	
	public Position getPositionB() {
		return pb;
	}

	public double getXA() {
		 return pa.getX();
	}

	public double getXB() {
	 return pb.getX();
	}

	public double getYA() {
	 return pa.getY();
	}

	public double getYB() {
		return pb.getY();
	}
	
}
