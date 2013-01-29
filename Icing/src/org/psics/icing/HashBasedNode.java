package org.psics.icing;

import java.util.ArrayList;
import java.util.HashMap;

import org.catacomb.interlish.structure.TreeChangeReporter;
import org.catacomb.interlish.structure.TreeNode;
import org.psics.project.StandaloneItem;


public class HashBasedNode implements TreeNode {


	String id;



	ArrayList<ItemNode> children;
	HashMap<String, ItemNode> itemHM;



	Object parent;

	public HashBasedNode(Object p, String s, HashMap<String, StandaloneItem> iHM) {
		parent = p;
		id = s;
		// E.info("new hash based node " + id);
		syncFromHash(iHM);
	}


	public String toString() {
		return id;
	}


	private void syncFromHash(HashMap<String, StandaloneItem> srcHM) {
		children = new ArrayList<ItemNode>();
		itemHM = new HashMap<String, ItemNode>();

		for (String s : srcHM.keySet()) {
			ItemNode nd = new ItemNode(this, s, srcHM.get(s));
			itemHM.put(s, nd);
			children.add(nd);
		}
	}



	public Object getChild(int index) {
		 return children.get(index);
	}

	public int getChildCount() {
		 return children.size();
	}

	public int getIndexOfChild(Object child) {
		return children.indexOf(child);
	}

	public Object getParent() {
		 return parent;
	}

	public boolean isLeaf() {
		 return false;
	}


	public boolean containsNode(String s) {
		 return (itemHM.containsKey(s));
	}

	public ItemNode getNode(String s) {
		return itemHM.get(s);
	}


	public void updateTo(HashMap<String, StandaloneItem> hm, TreeChangeReporter reporter) {

		for (String s : hm.keySet()) {
			 if (itemHM.containsKey(s)) {
				 // OK
			 } else {
				 ItemNode hbn = new ItemNode(this, s, hm.get(s));
				 children.add(hbn);
				 itemHM.put(s, hbn);
				 if (reporter != null) {
					 reporter.nodeAddedUnder(this, hbn);
					 // TODO do we need to add the subnodes - surely not?
				 }
			 }
		 }

		ArrayList<String> keys = new ArrayList<String>();
		keys.addAll(itemHM.keySet());

		 for (String s : keys) {
			 if (hm.containsKey(s)) {
				 // OK
			 } else {
				 ItemNode hbn = itemHM.get(s);
				 children.remove(hbn);
				 itemHM.remove(s);

				 if (reporter != null) {
					 reporter.nodeRemoved(this, hbn);
				 }
			 }
		 }

	}
}
