package org.psics.project;

import java.io.File;

import org.catacomb.be.StringIdentifiable;
import org.psics.be.CopyMaker;
import org.psics.be.Dummy;
import org.psics.be.E;
import org.psics.be.IDable;
import org.psics.model.ModelElement;
import org.psics.om.Serializer;
import org.psics.util.FileUtil;

// TODO don't want dependence on catacomb.be: move to some super-package?
public class StandaloneItem implements StringIdentifiable {

	File file;
	String id;
	Object object;


	Object edited;


	int extType;

	public StandaloneItem(String sid, File f, Object o) {
		id = sid;
		file = f;
		object = o;
	}


	public boolean hasObject() {
		boolean ret = false;
		if (object != null) {
			ret = true;
		}
		return ret;
	}


	public String getID() {
		return id;
	}

	public void setObject(Object obj) {
		object = obj;
		E.info("set object " + obj + " " + this + " " + (object instanceof Dummy));
	}


	public String getTypeSummary() {
		if (object instanceof Dummy) {
			E.override(" in " + this);
		}
		return object.getClass().getSimpleName();
	}

	public Object getObject() {
		Object ret = object;
		return ret;
	}


	public String getStringIdentifier() {
		return id;
	}


	public File getFile() {
		return file;
	}


	public void saveXMLObject() {
		writeXMLObject(object);
	}

	// this is called when we are saving an object from elsewhere into a StandaloneItem (save as)
	// which will the n be reinstantiated from the file
	public void saveXMLObjectAs(Object obj, String saveid) {
		if (obj instanceof IDable) {
			IDable x = (IDable)obj;
			String oldid = x.getID();
			x.setID(saveid);
			writeXMLObject(x);
			x.setID(oldid);
		} else {
			E.error("cant write non IDd object as " + saveid);
		}
	}


	public void writeXMLObject(Object obj) {
		if (obj.getClass().getPackage().getName().startsWith(ModelElement.class.getPackage().getName())) {
			String s = Serializer.serialize(obj);
			FileUtil.writeStringToFile(s, file);
		} else {
			E.error("cant save " + obj);
		}
	}


	public boolean hasEdited() {
		boolean ret = false;
		if (edited != null) {
			ret = true;
		}
		return ret;
	}


	public void setEdited(Object obj) {
		edited = obj;
	}

	public Object getEdited() {
		return edited;
	}


	public void setType(int k) {
		extType = k;
	}

	public int getType() {
		return extType;
	}

	  @SuppressWarnings("unchecked")
	public Object getCopyOfObject() {
		Object ret = null;
		if (object instanceof CopyMaker) {
			 ret = ((CopyMaker)object).makeCopy();
		} else {
			E.error("cant make copy of " + object);
		}
		return ret;
	}


	public void checkMatch(File f) {
		 if (file.equals(f)) {
			 // OK;
		 } else {
			 E.error("same id, different files? " + id + " " + f + " " + file);
		 }

	}
}
