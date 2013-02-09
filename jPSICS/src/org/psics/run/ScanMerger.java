package org.psics.run;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import java.io.File;

import org.psics.be.E;

public class ScanMerger {

	ArrayList<String> roots;
	String mergedRoot;
	
	File dir;
	
	
	public ScanMerger(File d, ArrayList<String> fileRoots, String mr) {
		dir = d;
		roots = fileRoots;
		mergedRoot = mr;
	}
	
	
	public void merge() {
		try {
		int nf = roots.size();
		String[] sa = roots.toArray(new String[nf]);
		BufferedReader[] readers = new BufferedReader[nf];
		StringBuffer sbm = new StringBuffer();
		 
		for (int i = 0; i < nf; i++) {
			readers[i] = new BufferedReader(new FileReader(new File(dir, sa[i] + ".txt")));
			sbm.append(sa[i] + ".txt ");
		}
		E.info("merging: " + sbm.toString());
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, mergedRoot + ".txt")));
		
		while (readers[0].ready()) {
			StringBuffer sb = new StringBuffer();
			String line = readers[0].readLine();
			sb.append(line);
			for (int i = 1; i < nf; i++) {
				String l = readers[i].readLine();
				l = l.trim();
				int ifs = l.indexOf(" ");
				sb.append(l.substring(ifs, l.length()));
			}
			sb.append("\n");
			bw.write(sb.toString());
		}
		
		bw.close();
		for (int i = 0; i < nf; i++) {
			readers[i].close();
		}
		
		
		} catch (IOException ex) {
			E.error("merging problem : " + ex);
		}
		
		  
	}

}
