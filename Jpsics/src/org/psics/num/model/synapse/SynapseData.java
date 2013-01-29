package org.psics.num.model.synapse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.num.ChannelGE;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;
import org.psics.util.TextDataWriter;


public class SynapseData {

	Time timestep = new Time(0.1, Units.ms);
	
	Temperature temp = null;
	

	ArrayList<String> orderedIds;
	HashMap<String, TableSynapse> synapseHM = new HashMap<String, TableSynapse>();
	boolean doneFix = false;

 
	public void setTimestep(Time dt) {
		timestep = dt;
	}

  
	public void buildTables() {
		for (TableSynapse tch : synapseHM.values()) {
			 
		}
	}

	public HashMap<String, Integer> getSynapseNumIDs() {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (String s : synapseHM.keySet()) {
			ret.put(s, synapseHM.get(s).getNID());
		}
		return ret;
	}
	
	
	public HashMap<String, TableSynapse> getSynapseHM() {
		return synapseHM;
	}

 



	public void fixOrder() {
		orderedIds = new ArrayList<String>();
	    orderedIds.addAll(synapseHM.keySet());
	    Collections.sort(orderedIds);

	    // set numeric ids for all synapses before serializing any since they can refer to each other
	    for (int i = 0; i < orderedIds.size(); i++) {
	    	TableSynapse tch = synapseHM.get(orderedIds.get(i));
	    	tch.setNID(i);
	    }
	}

	public void appendTo(TextDataWriter tdw) {
	    tdw.addInts(synapseHM.size());
	    tdw.addMeta("n synapse");


	    double rtemp = CalcUnits.getTemperatureValue(temp);
	    // now serialize them all
	    for (String sid : orderedIds) {
	    	TableSynapse tch = synapseHM.get(sid);
	    	tch.appendTo(tdw, rtemp);
 	    }

	}

	public void setTemperature(Temperature tp) {
		 temp = tp;

	}



	public void addTableSynapse(TableSynapse tsy) {
		if (doneFix) {
			E.error("cant add a channel after fixing order");
		}
		synapseHM.put(tsy.getID(), tsy);
	}
  


  
	public int getSynapseIndex(String ctyp) {
		 int ret = -1;
		 if (synapseHM.containsKey(ctyp)) {
			 ret = synapseHM.get(ctyp).getNID();
		 }
		 return ret;
	}

}
