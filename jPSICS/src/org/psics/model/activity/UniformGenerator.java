package org.psics.model.activity;

import org.psics.num.CalcUnits;
import org.psics.num.EventsConfig;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Frequency;
import org.psics.quantity.units.Units;

@ModelType(info = "A UniformGenerator provides simultaneous spikes to each " +
		"element of a population of synapses. The first batch of spikes are " +
		"delivered half a period after the start of the simulation", 
		standalone = false, tag = "Regular event generator for a synapse population", usedWithin = { AfferentEvents.class })
public class UniformGenerator extends EventSource {
	
	@Quantity(range = "[0, 1000)", required = true, tag = "event frequency per synapse", units = Units.Hz)
	public Frequency frequency = new Frequency();

	@Override
	public EventsConfig getEventsConfig() {
		EventsConfig econf = new EventsConfig(EventsConfig.UNIFORM);
		econf.setFrequency(CalcUnits.getFrequencyValue(frequency));
		
		return econf;
	}
}
