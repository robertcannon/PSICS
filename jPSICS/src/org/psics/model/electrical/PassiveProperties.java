package org.psics.model.electrical;

import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.BulkResistivity;
import org.psics.quantity.phys.SurfaceCapacitance;
import org.psics.quantity.units.Units;

@ModelType(info = "If supplied, passive properties objects can override the cell wide resistivity and " +
		"membrane capacitance for particular regions of the cell. A typical use is if certain parts " +
		"of the cell are known to have a larger surface area than is represented by the geometry.",
		standalone = false, tag = "Membrane and cytoplasm properties of for a region of the cell",
		usedWithin = { CellProperties.class })
public class PassiveProperties {


	@Identifier(tag = "identifier for this property set")
	public String id;

	@Label(info = "", tag = "Region to which tehse properties apply")
	public String region;


	@Quantity(range = "(0.8, 1.2)", required=false, tag="Membrane capacitance - capacitance per unit area genrally" +
			"around 1 uF per cm2 (equivalent to 0.01 pF per um2)",
			units = Units.uF_per_cm2)
	public SurfaceCapacitance membraneCapacitance;


	@Quantity(range = "(50, 200)", required=false, tag="Resistivity of contentents of the cell, also known as " +
			"axial resistivity",
			units = Units.ohm_cm)
	public BulkResistivity cytoplasmResistivity;


	public SurfaceCapacitance getCapacitance() {
		return membraneCapacitance;
	}


	public String getRegion() {
		return region;
	}


	public BulkResistivity getResistivity() {
		return cytoplasmResistivity;
	}




}
