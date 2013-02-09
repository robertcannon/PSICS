package org.psics.be;

import java.util.List;

import java.util.HashMap;

public class IDMap<G extends IDd> {

	HashMap<String, G> hMap;
	
	
	public IDMap(List<G> elts) {
		hMap = new HashMap<String, G>();
		
		for (G g : elts) {
			hMap.put(g.getID(), g);
		}
	}
	
	
	public boolean has(String s) {
		return (hMap.containsKey(s));
	}
	
	public G get(String s) {
		G ret = null;
		if (has(s)) {
			ret = hMap.get(s);
		}
		return ret;
	}
	
	
}
