package org.psics.model.display;

import org.psics.quantity.annotation.ModelType;

@ModelType(info = "A single line specified by two columns from a file", standalone = false,
		tag = "", usedWithin = { LineGraph.class })
public class Line extends LineSet {

}
