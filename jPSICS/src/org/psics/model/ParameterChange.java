package org.psics.model;

import org.psics.quantity.annotation.Label;


public class ParameterChange {

	@Label(info = "", tag = "The id of the parameter to be changed")
	public String to;

	@Label(info = "", tag = "The attribute of the target object that should be changed")
	public String attribute;

	@Label(info = "", tag = "The new text of the attribute")
	public String newText;



	public String getTargetID() {
		return to;
	}


	public String getAttributeName() {
		return attribute;
	}

	public String getNewText() {
		return newText;
	}
}
