package org.psics.model.activity;

import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.num.EventsConfig;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Frequency;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.units.Units;

@ModelType(info = "Poission distributed events for delivery to a population" +
		"of synapses. Each synapse receives an independent poisson event sequence " +
		"with the given mean frequency. Optionally, a local seed can be " +
		"specified so that this population always receives the same sequence of " +
		"events. If the seed is set, then the times and target synapses are " +
		"independent of the timestep used in the model.", 
		standalone = false, tag = "Poisson event generator", 
		usedWithin = { AfferentEvents.class })
public class PoissonGenerator extends EventSource {
	
	@Quantity(range = "[0, 1000)", required = true, tag = "event frequency per synapse", units = Units.Hz)
	public Frequency frequency = new Frequency();

	
	@IntegerNumber(range = "0, 100000", required = false, tag = "optional seed for this generator to give the " +
			"same event sequence each time (replicating the input exactly also requires a seed to be " +
			"set for the distribution of the corresponding synapse population)")
	public NDNumber seed = new NDNumber();


	@Override
	public EventsConfig getEventsConfig() {
		EventsConfig econf = new EventsConfig(EventsConfig.POISSON);
		econf.setFrequency(CalcUnits.getFrequencyValue(frequency));
		if (seed.valueSet()) {
			econf.setSeed(seed.getValue());
		}  
		return econf;
	}
	
}
