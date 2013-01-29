package org.psics.model.activity;

import org.psics.num.CalcUnits;
import org.psics.num.EventsConfig;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Frequency;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

public class ThresholdSensor extends EventSource {
	
	@Quantity(range = "(-50, 50)", required = true, tag = "threshold at which an event is fired", units = Units.mV)
	public Voltage threshold = new Voltage();

	@Override
	public EventsConfig getEventsConfig() {
		EventsConfig econf = new EventsConfig(EventsConfig.THRESHOLD);
		econf.setThreshold(CalcUnits.getVoltageValue(threshold));
		
		return econf;
	}
}
