package org.psics.model.activity;

import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.model.electrical.SynapsePopulation;
import org.psics.num.EventsConfig;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.SubComponent;

@ModelType(info = "AfferentEvent blocks are used to define afferent activity " +
		"arriving at a particular population of synapses. The activity can be " +
		"regular, randomly generated, or read from a file.", 
		standalone = false, tag = "External activity affecting " +
		"a single population of synapses.", usedWithin = {Activity.class})
public class AfferentEvents implements AddableTo {

	// TODO - we want this for the docs, but the population can't actually be found so we don't need 
	// to deref at present
	//@ReferenceByIdentifier(location = Location.global, required = false, 
	//		tag = "The population of synapses that the events arrive at.", targetTypes = { SynapsePopulation.class })
	public String population;
	
	@SubComponent(contentType = EventSource.class, tag = "Reference to the " +
			"event generator or input event sequence.")
	public EventSource evsource = null;
	

	// NB the parser sets this reference, but we don't use it: the EventsConfig object just knows the id 
	// and it is matched up later
	public SynapsePopulation r_population;
	
	
	public void add(Object obj) {
			if (obj instanceof EventSource) {
				if (evsource != null) {
					E.warning("multiple event sources in an AfferentEvents block? Only the last one will be used");
				}
				evsource = (EventSource)obj;
			}
	}


	public EventsConfig getEventsConfig(HashMap<String, Integer> popIDs) {
		EventsConfig ret = evsource.getEventsConfig();
		ret.setTargetPopID(popIDs.get(population));
		return ret;
		
	}
	
	
}
