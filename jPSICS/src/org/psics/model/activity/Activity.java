package org.psics.model.activity;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.Standalone;
import org.psics.model.control.CommandConfig;
import org.psics.num.AccessConfig;
import org.psics.num.Accessor;
import org.psics.num.ActivityConfig;
import org.psics.num.CalcUnits;
import org.psics.num.CompartmentTree;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.Logical;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;



@ModelType(info = "The Activity block describes the external activity affecting " +
		"synapses on a cell. It contains one or more AfferentEvent blocks, each " +
		"or which specifies the events arriving at a particular population " +
		"of synapses.", standalone = true,
		tag = "Incoming spike activity", usedWithin = { })
public class Activity implements AddableTo, Standalone {

	@Identifier(tag="Identifier (name) for the activity specification")
	public String id;

	@Container(contentTypes = {AfferentEvents.class}, tag = "")
	public ArrayList<AfferentEvents> eventPopulations = new ArrayList<AfferentEvents>();

	
	
	public void add(Object obj) {
		if (obj instanceof AfferentEvents) {
			eventPopulations.add((AfferentEvents)obj);
		} else {
			
			E.error("cant add " + obj); 
		}
		// TODO Auto-generated method stub
		
	}

	public String getID() {
		 return id;
	}

	
	public ActivityConfig getActivityConfig(HashMap<String, Integer> popIDs) {
		ActivityConfig aconf = new ActivityConfig();
		for (AfferentEvents aes : eventPopulations) {
			aconf.addEvents(aes.getEventsConfig(popIDs));
		}
		return aconf;
	}

 

}
