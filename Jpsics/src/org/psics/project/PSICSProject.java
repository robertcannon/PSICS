package org.psics.project;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.E;
import org.psics.be.TaskWatcher;
import org.psics.model.channel.KSChannel;
import org.psics.read.FileModelSource;
import org.psics.read.ModelSource;
import org.psics.util.FileUtil;

public class PSICSProject {

	File rootFolder;
	HashMap<String, StandaloneItem> itemHM;
	HashMap<String, Object> idoHM;
	TaskWatcher watcher;

	
	public PSICSProject(File f) {
		rootFolder = f;
	}


	public StandaloneItem getItem(String s) {
		StandaloneItem ret = null;
		if (itemHM != null) {
			if (itemHM.containsKey(s)) {
				ret = itemHM.get(s);
			} else {
				E.warning("no such item " + s);

				for (String ss : itemHM.keySet()) {
					E.info("possible item " + ss);
				}
			}
		}
		return ret;
	}



	public void read(TaskWatcher tw) {
		watcher = tw;
		idoHM = new HashMap<String, Object>();
		itemHM = new HashMap<String, StandaloneItem>();

		File[] fa = rootFolder.listFiles();

		// put the morphology files first;
		boolean[] bf = new boolean[fa.length];
		for (int i = 0; i < fa.length; i++) {
			File f = fa[i];
			bf[i] = false;
			if (f.getName().endsWith(".swc")) {
				bf[i] = true;
			} else if (f.getName().endsWith(".xml") && f.length() > 5000) {
				String head = FileUtil.readNLinesFromFile(f, 15);

				// special case for morphologies
				if (head.indexOf("<CellMorphology") >= 0 ||
					head.indexOf("<morphml") >= 0 ||
					(head.indexOf("<neuroml") >= 0 && head.indexOf("<cell ") >= 0)) {
					bf[i] = true;
				}
			}
		}


		ArrayList<File> af = new ArrayList<File>();
		for (int i = 0; i < fa.length; i++) {
			if (bf[i]) {
				af.add(fa[i]);
			}
		}
		for (int i = 0; i < fa.length; i++) {
			if (!bf[i]) {
				af.add(fa[i]);
			}
		}


		for (int i = 0; i < af.size(); i++) {
			if (watcher != null) {
				watcher.taskAdvanced(i / (0.9 * af.size()), "reading " + af.get(i).getName());
			}
			readFile(af.get(i));
		}



		for (String s : idoHM.keySet()) {
			File f = new File(rootFolder, s + ".xml");
			if (f.exists()) {
				if (itemHM.containsKey(s)) {
					itemHM.get(s).checkMatch(f);
				} else {
					StandaloneItem sit = new StandaloneItem(s, f, idoHM.get(s));
					itemHM.put(s, sit);
				}
			}
		}
		if (watcher != null) {
			watcher.taskAdvanced(1., "done, (" + fa.length + " files)");
		}

	}



	public void readNewFile(File f) {
		 String fid = readFile(f);
		 E.info("read new item " + fid);
	}



	private String readFile(File f) {
	String fnm = f.getName();
	String fid = null;
	if (fnm.endsWith(".xml")) {
		String froot = fnm.substring(0, fnm.length() - 4);
		fid = froot;

		if (idoHM.containsKey(froot)) {
			// must have been read while resolving another component;

		} else {
			boolean readImmediately = true;
		//	if (f.length() > 5000) {
				String head = FileUtil.readNLinesFromFile(f, 10);

				// special case for morphologies
				if ((head.indexOf("<morphml") >= 0) ||
					(head.indexOf("<neuroml") >= 0)) {
					StandaloneMorphologyItem smi = new StandaloneMorphologyItem(froot, f);
					itemHM.put(froot, smi);
					idoHM.put(froot, smi.getDummyObject());
					readImmediately = false;

				} else if (head.indexOf("<CellMorphology") >= 0 && f.length() > 10000) {
					// can also read plain psics files (that you get from exporting swc)
					// which tend to be long, but not ones using Branch elements,
					// so these are read normally
					StandaloneMorphologyItem smi = new StandaloneMorphologyItem(froot, f);
					itemHM.put(froot, smi);
					idoHM.put(froot, smi.getDummyObject());
					readImmediately = false;


				} else if (head.indexOf("<channelml") >= 0) {
					StandaloneChannelMLItem smi = new StandaloneChannelMLItem(froot, f);
					itemHM.put(froot, smi);
					idoHM.put(froot, smi.getDummyObject());
					readImmediately = false;


				} else {
					if (f.length() > 10000) {
						E.info("reading immediately " + f);
						E.warning("long file that isn't a cell morphology? " + f.length() + " " + head);
					}
				}
		//	}

			if (readImmediately) {
				ModelSource mSource = new FileModelSource(f);
				mSource.setItemIDMap(idoHM);
				mSource.read(true);
			}
		}

	} else if (fnm.endsWith(".swc")) {
		String froot = fnm.substring(0, fnm.length() - 4);
		fid = froot;
		itemHM.put(froot, new StandaloneSWCItem(froot, f));

	}
	return fid;
	}














	public HashMap<String, HashMap<String, StandaloneItem>> getCategorizedTreeHM() {
		HashMap<String, HashMap<String, StandaloneItem>> ret = new HashMap<String, HashMap<String, StandaloneItem>>();

		for (StandaloneItem sit : itemHM.values()) {
			String typ = sit.getTypeSummary();
			if (ret.containsKey(typ)) {

			} else {
				ret.put(typ, new HashMap<String, StandaloneItem>());
			}
			ret.get(typ).put(sit.getID(), sit);
		}
		return ret;
	}


	public String[] getChannelIDs() {
		ArrayList<String> wk = new ArrayList<String>();
		for (StandaloneItem sit : itemHM.values()) {
			if (sit.hasObject() && sit.getObject().getClass().equals(KSChannel.class)) {
				wk.add(sit.getID());
			}
		}
		return wk.toArray(new String[wk.size()]);
	}


	public void deleteItem(StandaloneItem sit) {
		itemHM.remove(sit.getID());
		idoHM.remove(sit.getID());
		sit.getFile().delete();
	}




}
