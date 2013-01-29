package org.psics.om;
 
import java.io.File;

import org.psics.util.FileUtil;
import org.psics.xml.XMLWriter;


public class ElementTest {


   public static void main(String[] argv) {
      
      File f = new File(argv[0]);

      String s = FileUtil.readStringFromFile(f);
      
      Object obj = OmReader.read(s);

      String sout = XMLWriter.serialize(obj);

      /*
      sout = sout.replaceAll("    ", "  ");
      sout = sout.replaceAll("Time", "abc");
      */

      System.out.println("pre length " + s.length() + ", post length " + sout.length());

    //TODO  DiffStrings.compareNonWhitespace(s, sout);
   }
   

}
