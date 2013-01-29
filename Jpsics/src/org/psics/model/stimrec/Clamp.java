package org.psics.model.stimrec;

import org.psics.be.E;
import org.psics.num.CommandProfile;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.annotation.StringEnum;
import org.psics.quantity.annotation.SubComponent;


public class Clamp extends DisplayableRecorder {

	@StringEnum(required = false, tag = "Value to use when the function changes during a step",
			values = "midpoint, average")
	public String stepValue;

	@SubComponent(tag="digitized time series data (optional)", contentType = TimeSeries.class)
	public TimeSeries timeSeries;


	@Flag(required = false, tag = "Specify whether to store the clamp value in output files")
	public boolean record = true;


	public int getStepStyle() {
		int ret = CommandProfile.MIDPOINT;

		String st = (stepValue == null ? "" : stepValue.trim().toLowerCase());
		if (st.length() == 0) {
			ret = CommandProfile.MIDPOINT;
			// TODO which should be defalut?

		} else if (st.equals("average")) {
			ret = CommandProfile.AVERAGE;

		} else if (st.equals("midpoint")) {
			ret = CommandProfile.MIDPOINT;

		} else {
			E.warning("unrecognized stepValue: " + st + " expecting 'midpoint' or 'average'");
		}
		return ret;
	}


	public boolean getRecordable() {
		return record;
	}

	public void setTimeSeries(TimeSeries ts) {
		if (timeSeries != null) {
			E.warning("two time series in one clamp?");
		} else {
			timeSeries = ts;
		}
	}


}
