package org.psics.model.environment;

import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;


@ModelType(info = "Ion definitions", standalone = false, tag = "", usedWithin = { CellEnvironment.class })
public class Ion {

	@Identifier(tag = "Symbol for the ion")
	public String id;
	
	
	@Label(tag="Full name for the ion", info = "")
	public String name;
	
	
	@Quantity(range = "(-100, 100)", required = false, tag = "Short-cut for specifying the electrical potential " +
			"for each ion instead of setting the concentrations and calculating it", units = Units.mV)
	public Voltage reversalPotential;


	public Voltage getDefaultReversal() {
		return reversalPotential;
	}


	public String getID() {
		return id;
	}
	
}
