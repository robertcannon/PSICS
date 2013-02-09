package org.psics.read;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.E;



public class ProxMap {

	public Object peer;
	String peerID;

	public ProxMap parent;

	// all child object;
	public ArrayList<ProxMap> children = new ArrayList<ProxMap>();

	// thos with ids;
	public HashMap<String, ProxMap> itemHM = new HashMap<String, ProxMap>();

	boolean doneResolve = false;



	public ProxMap(Object obj, ProxMap p) {
		peer = obj;
		parent = p;
		if (obj == null) {
			E.error("A required object is missing...");
		}
	}


	public void setResolved() {
		doneResolve = true;
	}

	public boolean resolved() {
		return doneResolve;
	}


	public Object getPeer() {
		return peer;
	}


	public void put(String s, ProxMap pm) {
		children.add(pm);
		if (s != null) {
			itemHM.put(s, pm);
		}
	}


	public boolean hasLocal(String s) {
		return (itemHM.containsKey(s));
	}


	public ProxMap getLocal(String s) {
		return itemHM.get(s);
	}


	public boolean has(String s) {
		 return (get(s) != null);
	}


	public ProxMap get(String s) {
		ProxMap pm = this;
		ProxMap ret = null;

		while(ret == null) {
			if (pm == null) {
				break;
			} else if (pm.hasLocal(s)) {
				ret = pm.getLocal(s);
				break;
			} else if (pm.getParent() != null){
				// try siblings
				for (ProxMap spm : pm.getParent().getChildren()) {
					if (spm != pm && spm.hasLocal(s)) {
						ret = spm.getLocal(s);
						break;
					}
				}
			}
			// still not found - go up one;
			pm = pm.parent;

		}
		return ret;
	}


	public void setPeerID(String sid) {
		 peerID = sid;

	}


	public ProxMap getParent() {
		return parent;
	}

	public ArrayList<ProxMap> getChildren() {
		return children;
	}

}
