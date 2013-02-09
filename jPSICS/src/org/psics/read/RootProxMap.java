package org.psics.read;

import java.util.ArrayList;



public class RootProxMap extends ProxMap {
	
	ArrayList<ProxMap> subPMs;
	
	public RootProxMap(Object obj) {
		super(obj, null);
		subPMs = new ArrayList<ProxMap>();
	}
	

	public ArrayList<ProxMap> getSubList() {
		return subPMs;
	}
	
}
