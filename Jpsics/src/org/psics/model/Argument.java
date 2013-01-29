package org.psics.model;

import org.psics.quantity.annotation.Label;


public class Argument {

	@Label(tag="Argument name as used in the code fragment", info="")
	public String name;


	@Label(tag="Argument type ('double', 'int', 'String' etc)", info="")
	public String type;


	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}


	public Argument makeCopy() {
		Argument ret = new Argument();
		ret.name = name;
		ret.type = type;
		return ret;
	}

}
