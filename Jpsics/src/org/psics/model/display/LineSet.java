package org.psics.model.display;

import org.psics.quantity.annotation.ModelType;

@ModelType(info = "A set of lines defining data to be plotted. The default representation is " +
		"to plot columns 1 throuhg N from the specified file against the first column, where " +
		"N is either the last column in the file, or the value of the maxshow attribute, if set",
		standalone = false,
		tag = "the data that goes into a plot", usedWithin = { LineGraph.class })
public class LineSet extends BaseLineSet {



}
