package org.psics.icing;

import java.awt.Color;

import org.catacomb.graph.gui.PickablePoint;
import org.psics.geom.Ball;
import org.psics.morph.TreePoint;


public final class IcingPoint {


	TreePoint treePoint;

	IcingPoint parent;

	double x;
	double y;
	double z;
	double r;

	double px;
	double py;
	double pz;
	double pr;

	boolean minor;

	public boolean ball;

	String partof = null;
	String label = "";

	PickablePoint pickable;

	Color color;

	boolean colored3d = false;

	public static final int AUTO = 0;
	public static final int TAPERED = 1;
	public static final int UNIFORM = 2;
	public int connectionStyle;


	public IcingPoint(TreePoint tp) {
		treePoint = tp;
		Ball b = tp.getBall();
		x = b.getX();
		y = b.getY();
		z = b.getZ();
		r = b.getRadius();
		minor = tp.minor;
		pickable = new PickablePoint(x, y, this);
		label = tp.getFirstLabel();
		partof = tp.getPartOf();
		color = Color.gray;
		ball = true;
	}


	public void checkDeBall() {
		if (parent != null) {
			double dx = px - x;
			double dy = py - y;
			double dz = pz - z;
			double d = Math.sqrt(dx*dx + dy*dy + dz*dz);
			if (d < r || (pr < r && pr * d <  r * r)) { // ADHOC
				ball = false;
			}
			if (d < pr || (!minor && r < pr && r * d < pr * pr)) {
				parent.ball = false;
			}
		}
	}


	public boolean isBall() {
		return ball;
	}


	public String getID() {
		return treePoint.getID();
	}

	public void setLabel(String s) {
		label = s;
	}

	public String toString() {
		return label + " " + String.format("(%d,%d,%d)", nr(x), nr(y), nr(z));
	}

	private int nr(double d) {
		return (int)(Math.round(d));
	}


	public void saveLabelToTreePoint() {
		if (label == null || label.trim().length() == 0) {
			treePoint.setLabel(null);
		} else {
			treePoint.setLabel(label);
		}
	}

	public void setSectionStyle(int ics) {
		connectionStyle = ics;
		if (ics == TAPERED) {
			treePoint.setNotMinor();
		} else if (ics == UNIFORM) {
			treePoint.setMinor();
		}
	}

	public String getLabel() {
		return label;
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

	public double getR() {
		return r;
	}

	public IcingPoint getParent() {
		return parent;
	}


	public void setParent(IcingPoint p) {
		parent = p;
		px = p.x;
		py = p.y;
		pz = p.z;

		if (minor) {
			pr = r;
		} else {
			pr = p.r;
		}

	}


	public boolean isMinor() {
		return minor;
	}


	public void setColored3d(boolean b) {
		colored3d = b;
	}

	public boolean isColored3d() {
		return colored3d;
	}


	public Color getColor() {
		return color;
	}

	public boolean taper() {
		return (connectionStyle == TAPERED);
	}

	public boolean uniform() {
		return (connectionStyle == UNIFORM);
	}

	public boolean auto() {
		return (connectionStyle == AUTO);
	}

}
