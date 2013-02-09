package org.psics.model.stimrec;

import org.psics.num.CommandProfile;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;


public abstract class ProfileFeature {

	@Identifier(tag = "optional identifier for use if the feature is to be modified")
	public String id = "";


	@Quantity(range = "[0, 1000)", required = false, tag = "start time of the pulse", units = Units.ms)
	public Time start = new Time();



	@Quantity(range = "[0, 5000)", required = false, tag = "If set, repeat the pulse at this interval measured from its start ", units = Units.ms)
	public Time repeatAfter = new Time();


	public abstract void exportTo(CommandProfile cp);


	public String getID() {
		return id;
	}

}
