package org.psics.geom;

public interface Ball extends Position {

	public double getRadius();

	public Ball makeCopy();

	public void setRadius(double radius);

	public void setWork(int iw);

	public int getWork();

	public void setRWork(double d);

	public double getRWork();

}

