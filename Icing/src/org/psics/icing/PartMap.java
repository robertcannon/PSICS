package org.psics.icing;

import java.awt.Color;

import org.catacomb.interlish.structure.Colored;
import org.psics.be.StringValued;


public class PartMap implements Colored, StringValued {

	String originalName;

	String name;

	String scolor;

	Color color;

	static int inc;
	static String[] colors = {"a00000", "00a000", "0000a0", "909000", "009090", "900090"};


	public PartMap(String n, String c) {
		name = n;
		originalName = n;

		String sc = null;
		if (c == null || c.trim().length() == 0) {
			sc = nextColor();
		} else {
			sc = c;
		}
		setColor(sc);
	}

	public String toString() {
		return name;
	}


	public String getOriginalName() {
		return originalName;
	}

	public String getStringValue() {
		return name;
	}

	private static String nextColor() {
		String ret = colors[inc];
		inc += 1;
		if (inc == colors.length) {
			inc = 0;
		}
		return ret;
	}


	public String getName() {
		return name;
	}

	public void setName(String s) {
		name = s;
	}

	public String getStringColor() {
		return scolor;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(String s) {
		if (s.startsWith("0x")) {
			// OK;
			scolor = s;
		} else {
			scolor = "0x" + s;
		}
		color = new Color(Integer.decode(scolor).intValue());
	}

}
