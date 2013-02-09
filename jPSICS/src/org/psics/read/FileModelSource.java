package org.psics.read;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.psics.be.DataFileSourced;
import org.psics.be.E;
import org.psics.be.FileSourced;
import org.psics.be.ImportException;
import org.psics.be.Standalone;
import org.psics.be.TextFileSourced;
import org.psics.be.Transitional;
import org.psics.util.FileUtil;
import org.psics.util.FormattedDataUtil;
import org.psics.xml.ReflectionInstantiator;
import org.psics.xml.XMLReader;

public class FileModelSource extends ModelSource {

	File rootFolder;

	File rootFile;
	String resourceName;
	XMLReader xmlReader;
	HashSet<String> pathHS;
	HashMap<String, LibraryItem> libItems = new HashMap<String, LibraryItem>();

	HashMap<String, Object> itemIDMap;


	// TODO - better way of handling this caching of files needed?
	// the application is for TimeSeries that just pick one column from a big file and 
	// we don't want to reread the file for each.
	static HashMap<File, double[][]> dataCache;

	{
		dataCache = new HashMap<File, double[][]>();

	}



	public FileModelSource(File f) {
		rootFile = f.getAbsoluteFile();
		rootFolder = rootFile.getParentFile();


		resourceName = f.getName();
		pathHS = new HashSet<String>();
	}


	public void setItemIDMap(HashMap<String, Object> hm) {
		itemIDMap = hm;
	}



	public Object simpleRead() {
		addLibrary(rootFolder);
		String s = FileUtil.readStringFromFile(new File(rootFolder, resourceName));
		ReflectionInstantiator refin = new ReflectionInstantiator();
		for (String pnm : getSearchPackageNames()) {
			refin.addSearchPackage(pnm);
		}
		xmlReader = new XMLReader(refin);
		Object ret = null;
		try {
			ret = xmlReader.read(s);
		} catch (Exception ex) {
			E.error("Cant read: - " + ex);
		}
		return ret;
	}



	public Object read(boolean bresolve) {
		addLibrary(rootFolder);
		String s = FileUtil.readStringFromFile(new File(rootFolder, resourceName));
		if (ccdir != null) {
			FileUtil.writeStringToFile(s, new File(ccdir, resourceName));
		}
		ReflectionInstantiator refin = new ReflectionInstantiator();
		for (String pnm : getSearchPackageNames()) {
			refin.addSearchPackage(pnm);
		}
		xmlReader = new XMLReader(refin);
		Object ret = null;
		try {
			ret = xmlReader.read(s);
		} catch (Exception ex) {
			E.error("Cant read: - " + ex);
		}




		if (ret instanceof Transitional) {
			// only during testing - single import item read and converted;
			try {
				ret = ((Transitional)ret).getFinal();
			} catch (ImportException ex) {
				E.error("cant read " + resourceName + " " + ex);
			}

		} else {
			// normal case - resolve references in one of our models;
			ResourceMap rm = new ResourceMap(ret);
			if (bresolve) {
				rm.resolve(this);
			}
		}



		if (ret instanceof Standalone) {
			if (itemIDMap != null) {
				String sid = ((Standalone)ret).getID();
				itemIDMap.put(sid, ret);
			//	E.info("directly added to item map : " + sid);
			}

		} else {
			E.warning("read non standalone cpt from file??" + ret);
		}


		return ret;
	}



	public boolean canGet(String id) {
		boolean bli = libItems.containsKey(id);

		return bli;
	}

	public String listItems() {
		return listItems(libItems);
	}

	public Object get(String id) {
		Object ret = null;
		LibraryItem li = libItems.get(id);
		ret = li.getObject();
		if (ret == null) {

			if (itemIDMap != null && itemIDMap.containsKey(id)) {
				ret = itemIDMap.get(id);
				li.setObject(ret);
				li.report();

			} else {

			String txt = li.getText();
			if (ccdir != null) {
				FileUtil.writeStringToFile(txt, new File(ccdir, li.getPath()));
			}
			try {
			if (isNeuron(txt)) {
				ReflectionInstantiator refin = new ReflectionInstantiator();
				for (String pnm : getNeuronSearchPackageNames()) {
					refin.addSearchPackage(pnm);
				}
				XMLReader nrnXMLReader = new XMLReader(refin);
				ret = nrnXMLReader.read(txt);
			} else {
				ret = xmlReader.read(txt);
			}

			while (ret instanceof Transitional) {
				try {
					ret = ((Transitional)ret).getFinal();
				} catch (ImportException ex) {
					E.error("cant read " + resourceName + " " + ex);
					break;
				}
			}

			li.setObject(ret);
			if (itemIDMap != null && !itemIDMap.containsKey(id)) {
				itemIDMap.put(id, ret);
			//	E.info("indirectly added to item map: " + id);
			}


			li.report();
			} catch (Exception ex) {
				E.error("cant read - " + ex);
			}
		}
		}
		return ret;
	}


	public void addLibrary(ProxMap pm, String spth) {
		// POSERR - called when we read an obhect with a LibraryPath - that
		// object could be coming from a differnt root folder - need path segment from there
		File fdir = null;
		if (spth.startsWith("/")) {
			fdir = new File(spth);
		} else {
			fdir = new File(rootFolder, spth);
		}
		addLibrary(fdir);

	}



	public void addLibrary(File fdir) {

		String abspath = fdir.getAbsolutePath();

		if (pathHS.contains(abspath)) {
			// already done;


		} else {
			pathHS.add(abspath);
			if (fdir.exists() && fdir.isDirectory()) {
			String[] reslist = FileUtil.getResourceList(fdir, ".xml");
				for (String s : reslist) {
					if (s.startsWith("/")) {
						s = s.substring(1, s.length());
					}
					LibraryItem li = new LibraryItem(fdir, s);
					String lid = li.getID();
					if (lid != null) {
						libItems.put(lid, li);
						// E.info("added to library " + lid);
					}
				}
		} else {
			E.warning("reference to nonexistent folder " + fdir);
		}
		}
	}


	public void populateFileSourced(FileSourced fs) {
		String sfnm = fs.getFileName();
		E.info("pop fs " + fs); 
		
		if (sfnm != null && sfnm.length() > 0) {
		File floc = new File(rootFolder, sfnm);
		File fpar = new File(rootFolder.getParentFile(), sfnm);
		if (!floc.exists() && fpar.exists()) {
			FileUtil.copyFile(fpar, floc);
		}


		if (fs instanceof DataFileSourced) {
			double[][] dat = null;
			if (dataCache.containsKey(floc)) {
				dat = dataCache.get(floc);

			} else {
				dat = FormattedDataUtil.readDataArray(floc);
				dataCache.put(floc, dat);
			}

			((DataFileSourced)fs).setData(dat);

		} else if (fs instanceof TextFileSourced) {
			String txt = FileUtil.readStringFromFile(floc);
			((TextFileSourced)fs).setText(txt);
		}
		}
	}





}
