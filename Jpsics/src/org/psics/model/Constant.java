package org.psics.model;

import org.psics.be.BodyValued;
import org.psics.model.math.EvaluationContext;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.Metadata;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;

public class Constant implements BodyValued {

	@Identifier(tag="Identifier of the constant - only needed if it is accessed externally")
	public String id;

	@Label(tag="name of the constant as used on the code fragment", info="")
	public String name;

	@Metadata(info = "", tag = "about the constant: purpose, units etc")
	public String info;


	@Quantity(units=Units.none, range="", required=true,
			tag="Value of the constant")
	public NDValue value;


	public String getName() {
		return name;
	}

	public double getValue() {
		return value.getNativeValue();
	}

	public Constant makeCopy() {
		Constant ret = new Constant();
		ret.id = id;
		ret.name = name;
		ret.info = info;
		ret.value = value.makeCopy();
		return ret;
	}

	public void set(EvaluationContext ectxt) {
		ectxt.addDouble(name, value.getNativeValue());
	}

	public void setBodyValue(String s) {
		double d = Double.parseDouble(s);
		value = new NDValue(d);
	}

}
