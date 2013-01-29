package org.psics.read;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

import org.psics.be.DataFileSourced;
import org.psics.be.E;
import org.psics.be.FileSourced;
import org.psics.be.ImportException;
import org.psics.be.TextFileSourced;
import org.psics.be.Transitional;
import org.psics.util.FileUtil;
import org.psics.util.FormattedDataUtil;
import org.psics.util.JUtil;
import org.psics.xml.ReflectionInstantiator;
import org.psics.xml.XMLReader;

public class ResourceModelSource extends ModelSource {

	Class<?> rootClass;
	String resourceName;

	XMLReader xmlReader;

	HashSet<String> pathHS;

	HashMap<String, LibraryItem> libItems = new HashMap<String, LibraryItem>();



	public ResourceModelSource(Class<?> cls, String rn) {
		rootClass = cls;
		resourceName = rn;
		pathHS = new HashSet<String>();
	}


	public void setItemIDMap(HashMap<String, Object> hm) {
		 E.missing("should we use this??");
	}

	public Object read(boolean bresolve) {
		addLibrary(rootClass, ".");
		String s = JUtil.getRelativeResource(rootClass, resourceName);
		if (ccdir != null) {
			FileUtil.writeStringToFile(s, new File(ccdir, resourceName));
		}
		// E.info("read " + resourceName);
		ReflectionInstantiator refin = new ReflectionInstantiator();
		for (String pnm : getSearchPackageNames()) {
			refin.addSearchPackage(pnm);
		}
		xmlReader = new XMLReader(refin);
		Object ret = null;

		try {
			ret = xmlReader.read(s);
		} catch (Exception ex) {
			E.error("cant parse - " + ex);
		}

		if (ret instanceof Transitional) {
			// only during testing - single import item read and converted;
			try {
				ret = ((Transitional)ret).getFinal();
			} catch (ImportException ex) {
				E.error("cant read " + resourceName + " " + ex);
			}


		} else {
			if (bresolve) {
				// normal case - resolve references in one of our models;
				ResourceMap rm = new ResourceMap(ret);
				rm.resolve(this);
			}
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
						E.error("cant read exception for " + ret + " " + resourceName + " " + ex);
						break;
					}
				}

			li.setObject(ret);
			li.report();
			} catch (Exception ex) {
				E.error("cant read " + id + " " + ex);
				ex.printStackTrace();
			}
		}
		return ret;
	}


	public void addLibrary(ProxMap pm, String spth) {
		addLibrary(pm.getPeer().getClass(), spth);
	}


	public void addLibrary(Class<?> cls, String pth) {

		String abspath = JUtil.absPath(cls, pth);

		if (pathHS.contains(abspath)) {
			// alredy done;


		} else {
			pathHS.add(abspath);

			String[] reslist = JUtil.getResourceList(cls, pth, ".xml");
				for (String s : reslist) {
					if (s.startsWith("/")) {
						s = s.substring(1, s.length());
					}
					LibraryItem li = new LibraryItem(cls, s);
					String lid = li.getID();
					if (lid != null) {
						libItems.put(lid, li);
						// E.info("added to library " + lid);
					}
				}
		}
	}



	public Object simpleRead() {
		addLibrary(rootClass, ".");
		String s = JUtil.getRelativeResource(rootClass, resourceName);
		ReflectionInstantiator refin = new ReflectionInstantiator();
		for (String pnm : getSearchPackageNames()) {
			refin.addSearchPackage(pnm);
		}
		xmlReader = new XMLReader(refin);
		Object ret = null;
		try {
			ret = xmlReader.read(s);
		} catch (Exception ex) {
			E.error("cant parse - " + ex);
		}

		return ret;
	}

	public void populateFileSourced(FileSourced fs) {
		E.info("pop fs " + fs);
			String sfnm = fs.getFileName();
			if (sfnm != null && sfnm.length() > 0) {
			if (fs instanceof DataFileSourced) {
				double[][] dat = FormattedDataUtil.readResourceDataArray(rootClass, sfnm);
				((DataFileSourced)fs).setData(dat);
		} else if (fs instanceof TextFileSourced) {
				String txt = JUtil.getRelativeResource(rootClass, sfnm);
			((TextFileSourced)fs).setText(txt);
		}
			}
	}

}
