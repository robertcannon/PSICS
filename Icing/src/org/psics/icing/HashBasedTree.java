package org.psics.icing;

import java.util.ArrayList;
import java.util.HashMap;

import org.catacomb.interlish.structure.Tree;
import org.catacomb.interlish.structure.TreeChangeReporter;
import org.catacomb.interlish.structure.TreeNode;
import org.catacomb.interlish.structure.TreeRoot;
import org.psics.be.E;
import org.psics.project.StandaloneItem;


public class HashBasedTree implements Tree, TreeRoot {

	HashMap<String, HashMap<String, StandaloneItem>> treeItemHM;

	ArrayList<HashBasedNode> children;

	String id;

	TreeChangeReporter reporter;

	public HashBasedTree(String sid, HashMap<String, HashMap<String, StandaloneItem>> tiHM) {
		 id = sid;
		 treeItemHM = tiHM;
		 syncFromHash();
	}


	public String toString() {
		return id;
	}


	private void syncFromHash() {
	//	E.info("syncing hash tree " + treeItemHM.size());
		children = new ArrayList<HashBasedNode>();
		 for (String s : treeItemHM.keySet()) {

			 HashMap<String, StandaloneItem> chm = treeItemHM.get(s);

			 children.add(new HashBasedNode(this, s, chm));
		 }
	}


	public Object[] getObjectPath(String s, boolean breq) {
		// TODO Auto-generated method stub
		Object[] ret = new Object[3];

		for (HashBasedNode hbn : children) {
			if (hbn.containsNode(s)) {
				ret[0] = this;
				ret[1] = hbn;
				ret[2] = hbn.getNode(s);
				break;
			}
		}
		if (ret[1] == null) {
			E.error("cant get object path for " + s);
		}
		return ret;
	}


	public Object[] getChildPath(String s) {
		Object[] ret = null;

		for (HashBasedNode hbn : children) {
			if (s.equals(hbn.id)) {
				ret = new Object[2];
				ret[0] = this;
				ret[1] = hbn;
				break;
			}
		}
		return ret;
	}





	public TreeNode getRoot() {
		return this;
	}


	public int getRootPolicy() {
		return TreeRoot.SHOW_ROOT;
	}


	public void setTreeChangeReporter(TreeChangeReporter tcr) {
		 reporter = tcr;

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
		return null;
	}


	public boolean isLeaf() {
		return false; // (children == null || children.size() == 0);
	}




	public void updateTo(HashMap<String, HashMap<String, StandaloneItem>> tiHM) {
		 HashMap<String, HashBasedNode> chHM = new HashMap<String, HashBasedNode>();
		 for (HashBasedNode ch : children) {
			 chHM.put(ch.id, ch);
		 }

		 for (String s : tiHM.keySet()) {
			 if (chHM.containsKey(s)) {
				 chHM.get(s).updateTo(tiHM.get(s), reporter);
			 } else {
				 HashBasedNode hbn = new HashBasedNode(this, s, tiHM.get(s));
				 children.add(hbn);
				 if (reporter != null) {
					 reporter.nodeAddedUnder(this, hbn);
					 // TODO do we need to add the subnodes - surely not?
				 }

			 }
		 }


		 for (String s :chHM.keySet()) {
			 if (tiHM.containsKey(s)) {
				 // OK
			 } else {
				 HashBasedNode hbn = chHM.get(s);
				 children.remove(hbn);
				 if (reporter != null) {
					 reporter.nodeRemoved(this, hbn);
				 }
			 }
		 }

	}


	public boolean hasChild(String s) {
		 	return (treeItemHM.containsKey(s));
	}

}
