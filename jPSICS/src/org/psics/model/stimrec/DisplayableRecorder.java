package org.psics.model.stimrec;

import org.psics.num.LineStyle;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Metadata;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.ReferenceByLabel;
import org.psics.quantity.annotation.StringEnum;
import org.psics.quantity.phys.Length;
import org.psics.quantity.phys.Pixels;
import org.psics.quantity.units.Units;


public class DisplayableRecorder {

	@Identifier(tag = "Identifier, also used as the column heading in output files unless a label is set.")
	public String id = null;

	@ReferenceByLabel(local = false, required = false, tag = "The attachment point on the morphology. The value should" +
			"match the id of the points on the structure where it is to be located.")
	public String at;

	@ReferenceByIdentifier(location = Location.local, required = false,
			tag = "Reference to a CellLocation definition", targetTypes = { CellLocation.class })
	public String location;
	public CellLocation r_location;

	@Quantity(range = "[0,1000)", required = false, tag = "Locate the recorder by specifying its distance " +
			" from the soma. Use in conjunctio with 'towards' if there is any potential ambiguity; and 'from'" +
			" to specify a distance from some other point. With 'from', negative distances are allowed and " +
			" define points nearer the soma.", units = Units.um)
	public Length distance;

	@ReferenceByLabel(local=false, required=false, tag="For use in conjunction with distance to specify an " +
			"alternative starting point.")
	public String from;


	@ReferenceByLabel(local=false, required=false, tag="For use in conjunction with distance to avoid ambiguity if " +
			"there are multiple points at the given distance. This should be the id or label of a point distal " +
			"to the target location.")
	public String towards;
	
	
	@Metadata(tag="Text to use for dolumn headings and file names", 
			info="This will be used for the column heading in output tables and for file names if the " +
					"splitColumns attribute is set on the main recording configuration. If it is not " +
					"specified, then a default name will be generated. Any spaces in the supplied text will" +
					"be replaced with underscores.")
	public String label;

	@Metadata(tag="Suggested color for plots", info="suggested color for displaying data - either a normal color or a hex value like #00ff00 (green)")
	public String lineColor;

	@StringEnum(required = false, tag = "Suggested line style for plots", values = "solid, dashed, dotted")
	public String lineStyle;


	@Quantity(range = "[0,5]", required = false, tag = "suggested line width for results", units = Units.none)
	public Pixels lineWidth;


	static int nextid = 1;
	
	
	public LineStyle getStyle() {
		double lw = 1;
		if (lineWidth != null) {
			lw = lineWidth.getNativeValue();
		}
		return new LineStyle(lineColor, lw, lineStyle);
	}



	public boolean hasLabelTarget() {
		return (at != null);
	}

	public boolean hasLocationTarget() {
		return (r_location != null);
	}

	public String getAt() {
		return at;
	}


	public CellLocation getLocation() {
		return r_location;
	}


	public String getID() {
		String ret = null;
		if (label != null) {
			ret = label;
		} else if (id != null) {
			ret = id;
		} else if (at != null) {
			ret = at;
		} else if (location != null) {
			ret = location;
		} else {
			ret = "dr_" + nextid;
			nextid += 1;
		}
		ret = ret.replaceAll(" ", "_");
		return ret;
	}



	public boolean hasDistance() {
		boolean ret = false;
		if (distance != null) {
			ret = true;
		}
		return ret;
	}


	public String getFrom() {
		return from;
	}

	public String getTowards() {
		return towards;
	}

	public Length getDistance() {
		return distance;
	}


}
