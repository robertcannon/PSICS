package org.psics.icing;

import org.catacomb.be.StringIdentifiable;
import org.catacomb.interlish.structure.TreeNode;
import org.psics.project.StandaloneItem;


public class ItemNode implements TreeNode, StringIdentifiable {

	String id;
	StandaloneItem item;

	HashBasedNode parent;

	public ItemNode(HashBasedNode hbn, String s, StandaloneItem it) {
		parent = hbn;
		id = s;
		item = it;

	//	E.info("new item node " + s);
	}


	public String toString() {
		return id;
	}


	public Object getChild(int index) {
		return null;
	}


	public int getChildCount() {
		return 0;
	}


	public int getIndexOfChild(Object child) {
		return -1;
	}


	public Object getParent() {
		 return parent;
	}


	public boolean isLeaf() {
		 return true;
	}


	public String getStringIdentifier() {
		 return item.getStringIdentifier();
	}

}
