package org.psics.model.morph;

import org.psics.geom.Position;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Length;
import org.psics.quantity.units.Units;


@ModelType(standalone=false, usedWithin={CellMorphology.class},
		tag="A branch perpendicular to the proximal segment of the parent process, terminating at " +
				"the specified position", info = "")
public class Branch extends Point {

	@Quantity(range = "(1, 20)", required = false, tag = "branch length", units = Units.um)
	public Length length;

	@Quantity(range = "(1, 50)", required = false,
			tag = "offset of attachment point from parent in proximal direction", units = Units.um)
	public Length offset;

	public boolean hasPosition() {
		Position p = getPosition();
		double xl = p.getX();
		double yl = p.getY();
		double zl = p.getZ();
		boolean ret = false;
		if (Math.abs(xl*xl + yl*yl + zl*zl) > 1.e-4) {
			ret = true;
		}
		return ret;
	}

	public boolean hasLength() {
		boolean ret = false;
		if (length != null && length.nonzero()) {
			ret = true;
		}
		return ret;
	}


	public boolean hasOffset() {
		boolean ret = false;
		if (offset != null && offset.nonzero()) {
			ret = true;
		}
		return ret;
	}

	public double getLength() {
		return length.getValue(Units.um);
	}

	public double getOffset() {
		return offset.getValue(Units.um);
	}


}
