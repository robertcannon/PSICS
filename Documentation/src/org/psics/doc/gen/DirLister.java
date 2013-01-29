package org.psics.doc.gen;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;



public class DirLister {



   public static void main(String[] argv) {
      dirList(new File(argv[0]));
   }



   public static void dirList(File d) {
      if (d.isDirectory()) {
	 File[] af = d.listFiles();
	 StringBuffer sbf = new StringBuffer();
	 StringBuffer sbd = new StringBuffer();
	 
	 for (int i = 0; i < af.length; i++) {
	    File f = af[i];
	    String fnm = f.getName();
	    
	    if (fnm.equals("_files.txt") || 
		fnm.equals("_directories.txt")) {
	       // ignore
     
	    } else { 
	       if (f.isFile()) {
		  sbf.append(fnm);
		  sbf.append("\n");
	       } else if (f.isDirectory()) {
		  sbd.append(fnm);
		  sbd.append("\n");
		  
		  dirList(f);
	       }
	    }
	 }
	 File ff = new File(d, "_files.txt");
	 writeStringToFile(sbf.toString(), ff);
	 
	 File fd = new File(d, "_directories.txt");
	 writeStringToFile(sbd.toString(), fd);
      }
   }




   
   public static boolean writeStringToFile (String sdat, File f) {
      String fnm = f.getName();
      boolean ok = false;
      if (f != null) {
	 try {
	    OutputStream fos = new FileOutputStream(f);
	    OutputStreamWriter osw = new OutputStreamWriter(fos);
	    
	    osw.write (sdat, 0, sdat.length());
	    osw.close();
	    ok = true;
	    
	 } catch (IOException ex) {
	    System.out.println("file writing error, trying to write file " +
			       fnm);
	    ex.printStackTrace();
	 }
      }
      return ok;
   }
   
}
