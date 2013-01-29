package org.psics.model.stimrec;

import org.psics.be.E;
import org.psics.model.channel.KSChannel;
import org.psics.num.Accessor;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.StringEnum;
import org.psics.quantity.phys.Length;
import org.psics.quantity.units.Units;

@ModelType(info = "Records conductance or current for a particular channel type " +
		"across part or all of the cell. This " +
		"gives access to quantities that are generally inaccessible experimentally (hence 'smart'). " +
		"If the range is specified, then only channels within that distance of the recording site are " +
		"included. Otherwise it applies to the whole cell.", standalone = false,
		tag = "Channel conductance or current recorder", usedWithin = { Access.class })
public class SmartRecorder extends DisplayableRecorder {


	@ReferenceByIdentifier(location = Location.global, required = true, tag = "The channel type to record fromn",
			targetTypes = { KSChannel.class })
	public String channel;
	public KSChannel r_channel;


	@StringEnum(required = false, tag = "Specify whether to record current or conductance",
			values = "current, conductance")
	public String record;




	@Quantity(range = "If specified, only channels within this distance from the recording site are included",
			required = false, tag = "range within which channels are recorded", units = Units.um)
	public Length range;




	public int getModality() {
		int ret = Accessor.CURRENT;
		if (record != null) {
			if (record.equals("current")) {
				 ret = Accessor.CURRENT;

			} else if (record.equals("conductance")) {
				ret = Accessor.CONDUCTANCE;

			} else {
				E.warning("unrecognized recording mode " + record + " execting 'current' or 'conductance'");
			}
		}
		return ret;

	}



	public String getChannelType() {
		return channel;
	}

	public double getRange() {
		double ret = -1;
		if (range != null) {
			ret = CalcUnits.getLengthValue(range);
		}
		return ret;
	}

}
