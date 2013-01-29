package org.psics.om;

import java.io.File;

import org.psics.be.Element;
import org.psics.util.FileUtil;


public class Serializer {

     public static String serialize(Object obj) {
    	 return serialize(obj, "%10.4g");

	}

   public static String serialize(Object obj, String format) {
      if (obj instanceof String) {
         return (String)obj;
      }


      OmElementizer elementizer = new OmElementizer(new SerializationContext());
      elementizer.setDefaultDoubleFormat(format);

      Element elt = null;

      if (obj instanceof Element) {
         elt = (Element)obj;
      } else {
         elt = elementizer.getElement(obj);
      }

      StringBuffer sb = new StringBuffer();
      String psk = "";
      XMLElementWriter.appendElement(sb, psk, elt);

      return sb.toString();
   }


   public static void writeToFile(Object obj, File f) {
	   String s = serialize(obj);
	   FileUtil.writeStringToFile(s, f);
   }


}
