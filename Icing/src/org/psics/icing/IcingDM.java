package org.psics.icing;


import java.io.File;
import java.util.HashMap;

import org.catacomb.druid.load.DruidAppBase;
import org.psics.be.E;
import org.psics.be.TaskWatcher;
import org.psics.project.PSICSProject;
import org.psics.project.StandaloneItem;


public class IcingDM implements Runnable {

	public static final String READ_FILE_TASK = "icingDM_read_file_task";

	static IcingDM instance;

	File rootDir;

	DruidAppBase appBase;

	HashMap<String, HashMap<String, StandaloneItem>> treeItemHM;

	HashBasedTree itemTree;

	PSICSProject pp;

	TaskWatcher taskWatcher;


	public static IcingDM getDM() {
		if (instance == null) {
			newStart();
		}
		return instance;
	}

	public static void newStart() {
		instance = new IcingDM();
	}


	public IcingDM() {



	}


	public StandaloneItem getItem(String s) {
		StandaloneItem ret = null;
		if (pp != null) {
			ret = pp.getItem(s);
		}
		return ret;
	}


	public void readDirectory(File fdir) {
		rootDir = fdir;
		pp = new PSICSProject(rootDir);
		pp.read(null);
		makeTrees();
	}

	private void makeTrees() {
		treeItemHM = pp.getCategorizedTreeHM();
		itemTree = new HashBasedTree(rootDir.getAbsolutePath(), treeItemHM);
	}


	private void updateTrees() {
		if (itemTree == null) {
			makeTrees();
		} else {
			treeItemHM = pp.getCategorizedTreeHM();
			itemTree.updateTo(treeItemHM);
		}
	}



	public void readFile(File f) {
		if (f.isDirectory()) {
			readDirectory(f);
		} else {
			readDirectory(f.getParentFile());
		}
	}

	public HashBasedTree getItemTree() {
		return itemTree;
	}


	public String[] getChannelIDs() {
		return pp.getChannelIDs();
	}

	public void newFileAppeared(File f) {
		 pp.readNewFile(f);
		 updateTrees();

	}

	public File getRootFolder() {
		return rootDir;
	}

	public void deleteItem(StandaloneItem item) {
		pp.deleteItem(item);
		updateTrees();

	}

	public void threadReadFile(File f, TaskWatcher w) {
		if (f.isDirectory()) {
			threadReadDirectory(f, w);
		} else {
			threadReadDirectory(f.getParentFile(), w);
		}
	}


	public void threadReadDirectory(File fdir, TaskWatcher w) {
		taskWatcher = w;
		rootDir = fdir;
		Thread thread = new Thread(this);
		try {
			thread.start();
		} catch (Exception ex) {
			E.error(" " + ex);
		}

	}


	public void run() {
		pp = new PSICSProject(rootDir);
		pp.read(taskWatcher);
		makeTrees();
		if (taskWatcher != null) {
			taskWatcher.taskCompleted(IcingDM.READ_FILE_TASK);
		}
	}


}
