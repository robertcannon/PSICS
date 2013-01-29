package org.psics.util;

import java.util.HashSet;


public class TextDataWriter {

	StringBuffer sbfile;
	StringBuffer sbdata;
	StringBuffer sbmeta;

	String lastmeta = "";

	HashSet<String> ecoHS;

	public TextDataWriter() {
		sbfile = new StringBuffer();
		sbdata = new StringBuffer();
		sbmeta = new StringBuffer();
		ecoHS = new HashSet<String>();
	}


	public void add(String s) {
		newRow();
		sbdata.append(s);
		newRow();
	}


	public void addFormattedRow(String s) {
		sbfile.append(s);
		sbfile.append("\n");
	}
	
	public void add(double... vals) {
		newRow();
		addRow(vals);
	}

	public void addRow(double... vals) {
		for (double d : vals) {
			sbdata.append(String.format(" %12.6g", d));
		}
	}

	public void addRow(double[] dat, int ia, int ib) {
		for (int i = ia; i <= ib; i++) {
			sbdata.append(String.format(" %12.6g", dat[i]));
		}
	}


	public void addInts(String sp, int... vals) {
		newRow();
		sbdata.append(sp);
		addRowInts(vals);
	}

	public void addInts(int... vals) {
		newRow();
		addRowInts(vals);
	}

	public void addRowInts(int... vals) {
		for (int i : vals) {
			sbdata.append(String.format(" %d", i));
		}
	}

	public void addMeta(String s) {
		sbmeta.append(s);
	}

	public void addEcoMeta(String s) {
		if (ecoHS.contains(s)) {
			// skip it;
		} else {
			ecoHS.add(s);
			sbmeta.append(s);
		}
	}

	public void clearEco() {
		ecoHS = new HashSet<String>();
	}


	public void newRow() {
		if (sbdata.length() > 0 || sbmeta.length() > 0) {
			int dl = sbdata.length() + 1;
			sbfile.append(sbdata.toString());
			sbfile.append(" ");
			for (int i = 0; i < 60 - dl; i++) {
				sbfile.append(" ");
			}
			if (sbmeta.length() > 0) {
				sbfile.append("//");
				sbfile.append(sbmeta.toString());
				lastmeta = sbmeta.toString();
			}
			sbfile.append("\n");
		}
		sbdata = new StringBuffer();
		sbmeta = new StringBuffer();
	}

	public void endRow() {
		newRow();
	}


	public String getText() {
		return sbfile.toString();
	}


}
