package org.psics.doc.gen;

import java.io.File;

import org.psics.util.FileUtil;



public class Fattener {



   public Fattener() {
   }

   public static void main(String[] argv) {
      (new Fattener()).fattenAll(argv[0], argv[1]);
   }



   public void fattenAll(String sftop, String sfout) {
      fattenAll(new File(sftop), new File(sfout), null, null);
   }




   private void fattenAll(File ftgt, File fout, String pathtoin, String uppathin) {
	   String pathto = pathtoin;
	   String uppath = uppathin;
      if (ftgt.getName().equals("CVS")) {
	 return;
      }

      if (ftgt.isFile()) {
	 fatten(ftgt, fout, pathto, uppath);

      } else {
	 if (fout.exists()) {
	    // ok
	 } else {
	    fout.mkdir();
	 }

	 File[] af = ftgt.listFiles();
	 if (pathto == null) {
	    pathto = "";
	 } else {
	    pathto += ftgt.getName() + "/";
	 }

	 if (uppath == null) {
	    uppath = "";
	 } else {
	    uppath = "../" + uppath;
	 }


	 for (int i = 0; i < af.length; i++) {
	    fattenAll(af[i], new File(fout, af[i].getName()), pathto, uppath);
	 }
      }
   }



   private void fatten(File ftgt, File fout, String path, String uppath) {
      if (ftgt.getName().equals("CVS")) {
	 return;
      }



      String snm = ftgt.getName();
      if (snm.endsWith(".xml")) {
	 String name = snm.substring(0, snm.length() - 4);

	 String stxt = FileUtil.readStringFromFile(ftgt);

	 StringBuffer sb = new StringBuffer();

	 sb.append("<file xmlns:bio=\"http://morphml.org/channelml/schema\" " +
			 "xmlns:meta=\"http://morphml.org/meta/schema\" " +
	 		"name=\"" + name +
		   "\" path=\"" + path +
		   "\" rootpath=\"" + uppath + "\">\n");
	 sb.append(stxt);
	 sb.append("</file>\n");

	 FileUtil.writeStringToFile(sb.toString(), fout);
      }
   }


}
