package org.psics.read;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.psics.be.E;
import org.psics.util.FileUtil;
import org.psics.util.JUtil;


public class LibraryItem {



	private String path;

	private String id;

	private String text;
	private Object object; // once the text has been parsed;


	public LibraryItem(Class<?> b, String p) {

		path = p;
		text = JUtil.getRelativeResource(b, path);
		checkContent(text);
	}

	public LibraryItem(File f, String p) {

		path = p;
		text = FileUtil.readStringFromFile(new File(f, p));
		checkContent(text);
	}


	public String getPath() {
		return path;
	}

	private void checkContent(String txt) {
		id = null;
		String etext = txt;

		etext = etext.trim();
		while (etext.startsWith("<!--")) {
			int ie = etext.indexOf("-->");
			if (ie > 0) {
				etext = etext.substring(ie+1, etext.length());
			} else {

				E.error("start of comment but no end? " + firstLines(etext));
				break;
			}
		}


		if (etext.length() > 0) {
			int smax = etext.length();
			if (smax > 100) {
				smax = 100;
			}
			String starttext = etext.substring(0, smax); // POSERR - id always an early attribute ??

			Pattern pat = Pattern.compile("\\sid\\s*=\\s*\"([^\"]*)\"");
		    Matcher matcher = pat.matcher("");
	        matcher.reset(starttext);
		    if (matcher.find()) {
		       String idval = matcher.group(1);
		     //  E.info("regex got id " + idval);
		       id = idval;
		    }
		}

		if (id == null) {
			if (ModelSource.isNeuron(etext)) {
				id = FileUtil.getRootName(path);
				// E.info("found a Neuron file as " + id);
			}

			// E.warning("No id found in library item? " + path + ": " + firstLines(etext));
		}
	}

	private String firstLines(String etext) {
		String spr = etext;
		if (spr.length() > 500) {
			spr = spr.substring(0, 500);
		}
		return spr;
	}


	public String getID() {
		return id;
	}


	public String getText() {
		return text;
	}


	public void setObject(Object obj) {
		object = obj;
	}

	public Object getObject() {
		return object;
	}




	public void report() {
		// TODO accumulating logger with periodic printing
		//	E.info("loaded library item " + id + " (" + path + ")");
	}


}
