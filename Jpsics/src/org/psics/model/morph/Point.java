package org.psics.model.morph;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.be.Element;
import org.psics.be.ElementFactory;
import org.psics.be.ElementWriter;
import org.psics.be.Elementizer;
import org.psics.geom.Geom;
import org.psics.geom.Position;
import org.psics.geom.Vector;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Length;
import org.psics.quantity.phys.PhysicalCoordinate;
import org.psics.quantity.units.Units;

@ModelType(standalone=false, usedWithin={CellMorphology.class},
		tag="A point on a process of a cell with a radius specifying the radius of the process", info = "")
public class Point implements ElementWriter {

	@Identifier(tag="Identifier for the point - unique within this cell")
	public String id;

	@Quantity(range = "(-1000, 1000)", required = true, tag = "x coordiante", units = Units.um)
	public PhysicalCoordinate x;

	@Quantity(range = "(-1000, 1000)", required = true, tag = "y coordiante", units = Units.um)
	public PhysicalCoordinate y;

	@Quantity(range = "(-1000, 1000)", required = true, tag = "z coordiante", units = Units.um)
	public PhysicalCoordinate z;

	@Quantity(range = "(0.1, 10)", required = true, tag = "radius", units = Units.um)
	public Length r;

	@Quantity(range = "(0.1, 10)", required = true, tag = "distance beyond parent point: " +
			"an alternative to specifying x,y,z coordinates", units = Units.um)
	public Length beyond;

	@Label(info="", tag = "The id of the parent point (nearer the soma)")
	public String parent;
	protected Point p_parent;

	@Label(info = "Labels can be used to tag points on the structure for later use in assigning channels etc",
			tag = "User defined label for the point")
	public String label;


	@Label(info = "The 'partof' attribute can be used to indicate which part of the cell " +
			"the point is in (axon, soma etc). Any text value is acceptable as a partof code, " +
			"although some files also have a numbering convention where 0=axon...(TBD)",
			tag = "Code defining which part of the cell the point belongs to")
	public String partof;


	@Flag(required = false, tag = "Make the segment to this point adopt the points radius rather than tapering from its parent")
	public boolean minor = false;

	@Flag(required = false, tag = "Translate this point and its children so that " +
			"it lies on the surface of the parent segment. Normally this means moving it " +
			"so it is one parent radius away from the parent point. If squareCaps is set " +
			"for the main model, then the point is moved to the parent point (it goes at the center" +
			"of the flat surface). This feature is mainly to provide comaptibility" +
			"with other simulators and to handle models where branches have been independently " +
			"digitized and may not align properly with their parents.")
	public boolean onSurface = false;

	
	private ArrayList<Point> r_children = new ArrayList<Point>();

	// private ArrayList<String> labels;


	public Point() {
		super();
	}



	public Point(String sid, double ax, double ay, double az, double ar) {
		id = sid;
		x = new PhysicalCoordinate(ax, Units.um);
		y = new PhysicalCoordinate(ay, Units.um);
		z = new PhysicalCoordinate(az, Units.um);
		r = new Length(ar, Units.um);
	}


	public Point(String sid, double x, double y, double z, double r, String parid) {
		 this(sid, x, y, z, r);
		 parent = parid;
	}

	public Point(String sid, double x, double y, double z, double r, String parid,
			String pof, String lbl) {
		this(sid, x, y, z, r, parid);
		if (pof != null) {
			setPartOfCode(pof);
		}

		if (lbl != null) {
			addLabel(lbl);
		}
	}

	public Point(String sid, Position pos, double ar, boolean min) {
		this(sid, pos.getX(), pos.getY(), pos.getZ(), ar);
		minor = min;
	}

	public void setLabel(String s) {
		label = s;
	}

	public String toString() {
		return ("(" + id + " " + (x != null ? x.getNativeValue() : "null") + ", " +
				(y != null ? y.getNativeValue() : "null") + ", " +
				(z != null ? z.getNativeValue(): "null") + ", r=" +
				(r != null ? r.getNativeValue() : "null") + ")");
	}


	public void setPosition(Position p) {
		x = new PhysicalCoordinate(p.getX(), Units.um);
		y = new PhysicalCoordinate(p.getY(), Units.um);
		z = new PhysicalCoordinate(p.getZ(), Units.um);
	}



	public boolean hasParent() {
		boolean ret = false;
		if (p_parent != null) {
			ret = true;
		}
		return ret;
	}


	public void setParent(Point ppar) {
		 p_parent = ppar;
		 parent = ppar.getID();
		 if (parent == null) {
			 E.error("Set a parent point that has no id??");
		 }
	}

	protected void addChild(Point pc) {
		r_children.add(pc);
	}

	public void removeChild(Point pc) {
		r_children.remove(pc);
	}

	public void deChild() {
		r_children.clear();
	}

	public Point getParent() {
		return p_parent;
	}

	public ArrayList<Point> getChildren() {
		return r_children;
	}

	public double getX() {
		 return x.getValue(Units.um);
	}

	public double getY() {
		 return y.getValue(Units.um);
	}

	public double getZ() {
		 return z.getValue(Units.um);
	}

	public double getR() {
		 return r.getValue(Units.um);
	}

	public String getID() {
		return id;
	}

	public boolean isMinor() {
		return minor;
	}


	public void setMinor() {
		minor = true;
	}

	public boolean isOnSurface() {
		return onSurface;
	}

	public void setPartOfCode(String s) {
		partof = s;
	}



	public void addLabel(String s) {
		if (label == null) {
			label = s;
		} else {
			E.warning("adding a label when allready labelled? " + label + " " + s);
		}
		/*
		if (labels == null) {
			labels = new ArrayList<String>();
		}
		labels.add(s);
		*/
	}



	public Position getPosition() {
		return Geom.position(x.getValue(Units.um), y.getValue(Units.um), z.getValue(Units.um));
	}

	public void setParentID(String pid) {
		 parent = pid;
		 p_parent = null;
	}

	public Element makeElement(ElementFactory ef, Elementizer eltz) {
		String fmt = "%.3f";
		Element ret = ef.makeElement("Point");
		ef.setOneLine(ret);
		ef.addAttribute(ret, "id", id);
		ef.addAttribute(ret, "x", x.getValue(Units.um), fmt);
		ef.addAttribute(ret, "y", y.getValue(Units.um), fmt);
		ef.addAttribute(ret, "z", z.getValue(Units.um), fmt);
		ef.addAttribute(ret, "r", r.getValue(Units.um), fmt);
		if (minor) {
			ef.addAttribute(ret, "minor", "true");
		}
		if (parent != null) {
			ef.addAttribute(ret, "parent", parent);
		} else if (p_parent != null) {
			ef.addAttribute(ret, "parent", p_parent.getID());
		}
		if (label != null) {
			ef.addAttribute(ret, "label", label);
		}

		/*
			} else {
				ef.addAttribute(ret, "label", labels.get(0));
				E.warning("leaving out multiple labels? " + labels.get(1));
				StringBuffer sb = new StringBuffer();
				for (String s : labels) {
					sb.append(s);
					sb.append(", ");
				}
				ef.addAttribute(ret, "label", sb.toString());
			}
		 */

		return ret;
	}

	public String getLabel() {
		return label;
	}

	public String getPartOf() {
		return partof;
	}

	public void partToLabel() {
		 addLabel(partof);
		 partof = null;
	}



	public boolean isRelative() {
		 return !isAbsolute();
	}



	public boolean isAbsolute() {
		 boolean ret = false;
		 if (x != null && y != null && z != null) {
			 ret = true;
		 }
		 return ret;
	}



	public void translate(Vector shift) {
		x.incrementNative(shift.getDX());
		y.incrementNative(shift.getDY());
		z.incrementNative(shift.getDZ());
	}

}

