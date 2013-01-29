package org.psics.model.display;

import org.psics.quantity.annotation.Flag;
import org.psics.quantity.annotation.ModelType;


@ModelType(info =  "Like the MeanVariance component, this has all the properties of a normal " +
		"LineSet, but instead of plotting hte data directly, it computes the PSD and plots that. ",
		standalone = false, tag = "Power spectrum plot", usedWithin = { ViewConfig.class })
public class PowerSpectrum extends BaseLineSet {


	@Flag(required = false, tag = "Returns log10(f) and log10(psd)")
	public boolean loglog = false;



	public boolean isLogLog() {
		return loglog;
	}

}
