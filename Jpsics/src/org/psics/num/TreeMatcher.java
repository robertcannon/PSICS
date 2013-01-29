package org.psics.num;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.psics.be.E;


public class TreeMatcher {

	CompartmentTree ctree;

	HashMap<String, Compartment> idHM;


	public static final int NOREGION = 0;
	public static final int WHERE = 1;
	public static final int PROXIMAL = 2;
	public static final int DISTAL = 3;



	public TreeMatcher(CompartmentTree ct) {
		ctree = ct;
	}

	public boolean[] getRegionMask(String match, int maskMode) {
		boolean[] bm = null;
		if (match.indexOf("*") < 0) {
			bm = simpleMatch(match);
		} else {
			bm = regexMatch(match);
		}
		int nr = 0;
		for (int i = 0; i < bm.length; i++) {
			if (bm[i]) {
				nr += 1;
			}
		}
		// E.info("regin mask returning " + nr + " matches for " + match + " from " + bret.length + " cpts");
		if (nr == 0) {
			E.warning("no matches for " + match + " on structure. All labels:\n" + allLabels());
		}
		boolean[] bret = null;
		if (maskMode == WHERE) {
			bret = bm;

		} else if (maskMode == PROXIMAL) {
			bret = ctree.getProximalPoints(bm);

		} else if (maskMode == DISTAL) {
			bret = ctree.getDistalPoints(bm);
		}
		return bret;
	}


	public String allLabels() {
		StringBuffer sb = new StringBuffer();
		int nl = 0;
		for (Compartment cpt : ctree.getCompartments()) {
			HashSet<String> labs = cpt.getLabels();
			if (labs != null) {
				sb.append("(");
				for (String s : labs) {
					sb.append(s);
					sb.append(" ");
					nl += 1;
					if (nl == 10) {
						sb.append("\n");
						nl = 0;
					}
				}
				sb.append("), ");
			}
		}
		return sb.toString();
	}



	private boolean[] simpleMatch(String match) {
		boolean[] ba = new boolean[ctree.size()];
		int icpt = 0;
		for (Compartment cpt : ctree.getCompartments()) {
			ba[icpt] = labelMatches(cpt.getLabels(), match);
			icpt += 1;
		}
		return ba;

	}
	private boolean labelMatches(HashSet<String> labels, String match) {
		boolean ret = false;
		if (labels != null && labels.size() > 0) {
			for (String lab : labels) {
				if (lab.equals(match)) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}


	private boolean[] regexMatch(String match) {


		Matcher matcher = makeMatcher(match);

		boolean[] ba = new boolean[ctree.size()];
		int icpt = 0;
		for (Compartment cpt : ctree.getCompartments()) {
			HashSet<String> hs = cpt.getLabels();
			ba[icpt] = false;
			for (String s : hs) {
				matcher.reset(s);
				if (matcher.matches()) {
					ba[icpt] = true;
				}
			}

			icpt += 1;

		}
		return ba;
	}


	private Matcher makeMatcher(String match) {
	String rex = match;
	rex = rex.replaceAll("\\.", "\\\\.");
	rex = rex.replaceAll("\\[", "\\\\[");
	rex = rex.replaceAll("\\]", "\\\\]");
	rex = rex.replaceAll("\\)", "\\\\)");
	rex = rex.replaceAll("\\(", "\\\\(");
	rex = rex.replaceAll("\\*", ".\\*");

	Pattern p = Pattern.compile(rex);
	return p.matcher("");
	}



	private void matchPair(String match, String[] txt) {
		Matcher m = makeMatcher(match);
		for (String s : txt) {
			m.reset(s);
			E.info(match + "   " + s + "   " + m.matches());
		}
	}

	public static void main(String[] argv) {
		TreeMatcher tm = new TreeMatcher(null);
		String[] sm = {"abc", "*abc", "a*bcd*ef", "cat*.txt", "cat*", "cat[*]", "*cat*"};
		String[] stry = {"abc", "abcd", "catsabc", "aaaaabcdandef", "abcd00ef", "abef", "cat3", "xcat3",
					"cat[123]", "thecat[2]", "thecats", "cats.txt", "catsanddogs.txt"};

		for (String s : sm) {
			tm.matchPair(s, stry);
		}
	}

	public Compartment getIdentifiedCompartment(String atid) {
		Compartment ret = null;
		if (atid.indexOf("*") < 0) {
			ret = getExactMatchCompartment(atid);
		} else {
			boolean[] ba = regexMatch(atid);
			int ntr = ntrue(ba);
			if (ntr == 0) {
				// will return null - caller can handle it;
			} else {
				ret = ctree.getIthCompartment(itrue(ba));
				if (ntr > 1) {
					E.oneLineWarning("multiple compartments matching " + atid + " (" + ntr + ") attaching to first match");
				}
			}
		}
		return ret;
	}


	private int ntrue(boolean[] ba) {
		int n = 0;
		for (int i = 0; i < ba.length; i++) {
			if (ba[i]) {
				n += 1;
			}
		}
		return n;
	}


	private int itrue(boolean[] ba) {
		int ret = -1;
		for (int i = 0; i < ba.length; i++) {
			if (ba[i]) {
				ret = i;
			}
		}
		return ret;
	}


	public void addIDd(Compartment cpt) {
		if (idHM != null) {
			idHM.put(cpt.getID(), cpt);
		}
	}

	private Compartment getExactMatchCompartment(String atid) {
		// labels that only occur on one cpt are treated as ids - POSERR
		if (idHM == null) {
			idHM = new HashMap<String, Compartment>();
			for (Compartment cpt : ctree.getCompartments()) {
				for (String s : cpt.getLabels()) {
					if (idHM.containsKey(s)) {
						// leave the first visited label;
					} else {
						idHM.put(s, cpt);
					}
				}
				if (cpt.getID() != null) {
					 // E.info("added cpot with uid " + cpt.getID());
					idHM.put(cpt.getID(), cpt);
				}
			}
		}

		Compartment ret = null;
		if (idHM.containsKey(atid)) {
			ret = idHM.get(atid);
		}
		return ret;
	}



	public void deMatch(Compartment cpt, String sm) {
		if (sm.indexOf("*") < 0) {
			cpt.getLabels().remove(sm);

		} else {
			Matcher matcher = makeMatcher(sm);
			ArrayList<String> lm = new ArrayList<String>();
			for (String s : cpt.getLabels()) {
				matcher.reset(s);
				if (matcher.matches()) {
					lm.add(s);
				}
			}
			cpt.getLabels().removeAll(lm);
		}
	}



}
