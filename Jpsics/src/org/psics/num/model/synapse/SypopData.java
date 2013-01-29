package org.psics.num.model.synapse;

import java.util.HashMap;

import org.psics.util.TextDataWriter;


public class SypopData {

	String[] popIDs;
	String[] typeIDs;
	int[] typeNumIDs;
	
	public SypopData(int size) {
		popIDs = new String[size];
		typeIDs = new String[size];
		typeNumIDs = new int[size];
	}

	
	public void addPopulation(int numID, String popid, String typeID, int tnid) {
		// TODO Auto-generated method stub
		popIDs[numID] = popid;
		typeIDs[numID] = typeID;
		typeNumIDs[numID] = tnid;	
	}

	
	public HashMap<String, Integer> getNumIDMap() {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (int i = 0; i < popIDs.length; i++) {
			ret.put(popIDs[i], i);
		}
		return ret;
	}

	
	public void appendTo(TextDataWriter tdw) {
		tdw.addInts(popIDs.length);
		tdw.addMeta("number of synapse populations");
		tdw.addInts(typeNumIDs);
		tdw.addMeta("synapse type codes for each population");
	}

}
