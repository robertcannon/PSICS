package org.psics.model;

import org.psics.be.E;
import org.psics.quantity.DimensionalQuantity;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.Metadata;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;

public class Parameter {
	
	@Identifier(tag = "")
	@Label(tag="name of the parameter, as used elsewhere in the model", info="")
	public String name;

	@Metadata(info = "", tag = "about the parameter: purpose, units etc")
	public String info;


	@Quantity(units=Units.none, range="", required=true,
			tag="Value of the parameter")
	public NDValue value;

	
	public String getName() {
		return name;
	}
	
	public double getDoubleValue() {
		E.info("getting param value for param " + name + " " + value);
		return value.getValue();
	}

	public DimensionalQuantity getValueDQ() {
		 return value;
	}
}
