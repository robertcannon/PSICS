package org.psics.out;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.E;
import org.psics.util.FileUtil;
import org.psics.util.TextDataWriter;
import org.psics.util.StringUtil;


public class ResultsWriter {

	   File rootFile;
	   File outputFile;

	   public final static int OUT = 0;
	   public final static int TEXT = 1;
	   public final static int BINARY = 2;

	   int type;

	   OutputStreamWriter writer;

	   boolean ready;

	   boolean open;

	   HashMap<String, ResultsWriter> siblings = new HashMap<String, ResultsWriter>();


	   public ResultsWriter(File f) {
		   rootFile = f;
		   outputFile = f;
		   type = TEXT;
		   ready = false;
		   try {
	            writer = new OutputStreamWriter(new FileOutputStream(outputFile));
	            ready = true;
	            open = true;
	         } catch (Exception ex) {
	            E.error("cant create file writer " + ex);
	         }
	   }




	   public ResultsWriter(File rFile, String sfx, int typ) {
	      rootFile = rFile;
	      type = typ;
	      outputFile =  getSiblingFile(sfx, typ);
	      ready = false;

	      if (type == TEXT || type == OUT) {
		         try {
		            writer = new OutputStreamWriter(new FileOutputStream(outputFile));
		            ready = true;
		            open = true;
		         } catch (Exception ex) {
		            E.error("cant create file writer " + ex);
		         }
	      } else {
		         E.error("binary not handled yet...");
	       }
	      // E.longInfo("created writer to " + outputFile.getAbsolutePath());
	   }


	   public String toString() {
		   return ("results writer: " + outputFile.getAbsolutePath());
	   }


	   private File getSiblingFile(String sfx, int stype) {
		   File ret = new File(rootFile.getParentFile(),
	    		  rootFile.getName() + sfx + "." + getTypeExtension(stype));
		   // get root name is confised if there are dots in name - should always
		   // supply a proper root to ResultsWriter (no extension).
		   //   FileUtil.getRootName(rootFile) + sfx + "." + getTypeExtension(stype));
		   return ret;
	   }

	   public File getSiblingFile(String sfx, String extn) {
		   File ret = new File(rootFile.getParentFile(),
	    		  	// FileUtil.getRootName(rootFile)
	    		  	rootFile.getName() + sfx + "." + extn);
		   return ret;
	   }




	   public File getFile() {
		   return outputFile;
	   }


	   private String getTypeExtension (int ty) {
		   String ret = "err";
		   if (ty == OUT) {
			   ret = "out";
		   } else if (ty == TEXT) {
			   ret = "txt";
		   } else if (ty == BINARY) {
			   ret = "dat";
		   }
		   return ret;
	   }



	   public void initMagic(String magic) {
		   try {
		   writer.write(magic + "\n");
		   } catch (IOException ex) {
			   E.error("exception writing magic no " + ex);
		   }
	   }

	   public void writeString(String sdat) {
	      if (ready) {
	         try {
	            writer.write(sdat, 0, sdat.length());
	         } catch (Exception ex) {
	            E.error("cant write: " + ex);
	         }
	      }
	   }



	   public void close() {
	     if (open) {
		 if (ready) {
	         try {
	        	writer.flush();
	            writer.close();
	         } catch (Exception ex) {
	            E.error("ex " + ex);
	         }
	      } else {
	         E.error("data not written (earlier errors)");
	      }
	     }
	   }


	   public void closeSiblings() {
	      if (siblings != null) {
	         for (ResultsWriter rw : siblings.values()) {
	            rw.close();
	         }
	      }
	   }



	   public void initSibling(String sfx, int stype) {
		   	ResultsWriter rw = new ResultsWriter(rootFile, sfx, stype);
		    if (siblings == null) {
		         siblings = new HashMap<String, ResultsWriter>();
		     }
		    siblings.put(sfx, rw);
	   }

	   public ResultsWriter getSibling(String sfx, int stype) {
		   if (!siblings.containsKey(sfx)) {
			   initSibling(sfx, stype);
		   }
		   return siblings.get(sfx);
	   }



	   public void closeData(String s) {
		   if (siblings.containsKey(s)) {
			   siblings.get(s).close();
		   } else {
			   E.warning("no such sibling file " + s);
		   }
	   }




	   public void writeToSiblingFile(String txt, String extn) {
	      writeToSiblingFile(txt, extn, TEXT);
	   }

	   public void writeToSiblingFile(String txt, String extn, int stype) {
	     ResultsWriter rw = getSibling(extn, stype);
	     rw.writeString(txt);
	   }


	public void writeDataNames(String snm, String sabscissa, String[] names) {
		String spref = ("tableColumnNames " + snm + " " + (names.length + 1) + "\n");
		StringBuffer sb = new StringBuffer();
		sb.append(sabscissa.replace(" ", "_"));
		sb.append(" ");
		for (String s : names) {
			sb.append(s.replace(" ", "_"));
			sb.append(" ");
		}
		sb.append("\n");
		writeString(spref + sb.toString());
	}



	public void writeDataNames(String sabscissa, String[] names) {
		StringBuffer sb = new StringBuffer();
		sb.append(sabscissa.replace(" ", "_"));
		sb.append(" ");
		for (String s : names) {
			sb.append(s.replace(" ", "_"));
			sb.append(" ");
		}
		sb.append("\n");
	    writeString(sb.toString());
	}


	public void writeData(String snm, double abscissa, double[] vals) {
		String spref = ("tableRow " + snm + " " + (vals.length + 1) + "\n");
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%10.6g", abscissa));
		sb.append(" ");
		for (double d : vals) {
			sb.append(String.format("%10.6g", d));
			sb.append(" ");
		}
		sb.append("\n");
		writeString(spref + sb.toString());
	}



	public void writeData(double abscissa, double[] vals) {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%10.6g", abscissa));
		sb.append(" ");
		for (double d : vals) {
			sb.append(String.format("%10.6g", d));
			sb.append(" ");
		}
		sb.append("\n");
		writeString(sb.toString());
	}


	public void appendTo(TextDataWriter tdw) {
		String sp = FileUtil.absoluteRoot(rootFile);
		if (sp.length() > 128) {
			E.error("output file path is too long (> 128 characters)");
			sp = sp.substring(0, 128);
		}
		tdw.add(StringUtil.blankPad(sp, 128));
		tdw.endRow();
	}




	// REFAC move to fileutil?

	public void mergeSiblings(ArrayList<String> resnames, String sfx, int stype) {
		ResultsWriter rw = new ResultsWriter(rootFile, sfx, stype);

		try {

		int nsource = resnames.size();
		BufferedReader[] bra = new BufferedReader[nsource];
		for (int i = 0; i < nsource; i++) {
			bra[i] = new BufferedReader(new FileReader(getSiblingFile(resnames.get(i), stype)));
		}


		BufferedReader b0 = bra[0];

		while (b0.ready()) {
			StringBuffer sb = new StringBuffer();
			sb.append(b0.readLine());
			for (int i = 1; i < nsource; i++) {
				sb.append(" ");
				String line = bra[i].readLine();
				line = line.trim();
				line = line.substring(line.indexOf(" ")+1, line.length());
				sb.append(line);
			}
			sb.append("\n");
			rw.writeString(sb.toString());
		}
		rw.close();


		} catch (Exception ex) {
			E.error("file merge failed : " + ex);
		}
	}

}




