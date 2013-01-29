package org.psics.num;

import java.util.ArrayList;

import org.psics.util.TextDataWriter;


public class ActivityConfig {

	ArrayList<EventsConfig> configs = new ArrayList<EventsConfig>();
	
	
	public void appendTo(TextDataWriter tdw) {
		tdw.addInts(configs.size());
		tdw.addMeta("number of event generators");
		for (EventsConfig ec : configs) {
			ec.appendTo(tdw);
		}
	}

	public void addEvents(EventsConfig eventsConfig) {
		configs.add(eventsConfig);
	}

}
