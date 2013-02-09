package org.psics.om;

import java.io.File;

import org.psics.be.E;
import org.psics.be.Element;
import org.psics.util.FileUtil;
import org.psics.xml.XMLReader;


public class OmReader {




	public static Element readFile(File f) {
	   Element ret = null;


	   if (f.exists()) {

	      String ftxt = FileUtil.readStringFromFile(f);
	      ret = read(ftxt);

	   } else {
	      E.error("no such resource file " + f);
	   }
	   return ret;
	}


	public static Element read(String s) {
		ElementConstructor ein = new ElementConstructor();

		XMLReader reader = new XMLReader(ein);

		Object obj = null;

		try {
			obj = reader.readObject(s);
		} catch (Exception ex) {
			E.error("cant parse: " + ex);
		}
		return (Element)obj;
	}
}
