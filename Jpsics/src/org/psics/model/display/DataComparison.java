package org.psics.model.display;

import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.ReferenceToFile;
import org.psics.quantity.phys.NDNumber;


@ModelType(info = "If a DataComparison block is included in a LineGraph, then as well as any " +
		"views that are defined, a table will be generated showing the mean and standard deviation" +
		"of the differences between the line data and the reference data in the specified file",
		standalone = false,
		tag = "Numerical comparison with reference data", usedWithin = { LineGraph.class })
public class DataComparison {

	@ReferenceToFile(fallback = "", required = false, tag = "")
	public String file;

	@IntegerNumber(range = "(1,)", required = false, tag = "The column to use for the comparison if there" +
			"are more than two columns in the file")
	public NDNumber line = new NDNumber(1);

	@Label(info = "optional rescaling of reference data to make it compatible with the generated results",
			tag = "rescaling vector")
	public String rescale;


	public String getFileName() {
		return file;
	}

	public String getRescaling() {
		return rescale;
	}

	public int getLine() {
		return line.getNativeValue();
	}



}

