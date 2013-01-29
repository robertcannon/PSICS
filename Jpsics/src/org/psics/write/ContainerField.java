package org.psics.write;

import java.util.ArrayList;

import org.psics.be.ContainerForm;
import org.psics.be.E;

public class ContainerField extends WritableField {

	ArrayList<Object> value;
	ContainerForm ann;

	public ContainerField(String s, double p, ArrayList<Object> v, ContainerForm tf) {
		super(s, p);
		value = v;
		ann = tf;

	}



	public String write(ListPosition lp) {
		StringBuffer sb = new StringBuffer();

		// String txt = ann.label();

		if (value.size() == 0) {
			// leave it out entirely
		} else if (value.size() == 1 && ann.unwrapone()) {
			sb.append(tlz.makeBlock(value.get(0)));

		} else {
			if (lp == ListPosition.FIRST) {

			} else if (lp == ListPosition.INNER) {
				sb.append(",");
			} else if (lp == ListPosition.LAST) {
				sb.append(" and");
			}

			int nv = value.size();
			if (nv == 1) {
				sb.append(" has one " + singular(ann.label()) + ". ");
				sb.append(tlz.makeBlock(value.get(0)));
			} else {
				sb.append(" has " + nv + " " + plural(ann.label()) + ". ");
				for (Object ob : value) {
					sb.append(tlz.makeBlock(ob));
				}
			}

			if (lp == ListPosition.SOLE || lp == ListPosition.LAST) {
				sb.append(". ");
			}

		}


		return sb.toString();
	}

/*
	private String intext(String txt, String val) {
		String ret = "";
		if (txt.indexOf("$") >= 0) {
			ret = " " + txt.replace("$", val);

		} else {
			ret = " has " + txt + " " + val + ",\n";
		}
		return ret;
	}
*/


	private static String singular(String txt) {
		String s = txt.replaceAll("\\[([^\\|]*)\\|([^\\]]*)\\]", "$1");
		return s;
	}


	private static String plural(String txt) {
		String s = txt.replaceAll("\\[([^\\|]*)\\|([^\\]]*)\\]", "$2");
		return s;
	}


	public static void main(String[] argv) {
		String[] tsts = {"str1[sing|plur]", "str2[|s]"};
		for (String s : tsts) {
			E.info("raw: " + s + "   sing=" + singular(s) + " pl=" + plural(s));
		}
	}


}
