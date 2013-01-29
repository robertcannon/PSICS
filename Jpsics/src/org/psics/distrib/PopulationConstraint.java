package org.psics.distrib;

import org.psics.num.TreeMatcher;


public class PopulationConstraint {

	public static final int NONE = 0;
	public static final int INCLUDE = 1;
	public static final int EXCLUDE = 2;
	public static final int RESTRICT = 3;
	int mode;

	public String condition;

	String region = null;


	int regionMode = TreeMatcher.WHERE;


	public PopulationConstraint(int m, String cond) {
		mode = m;
		setCondition(cond);
	}


	public PopulationConstraint() {
		 this(INCLUDE, "");
	}


	public String toString() {
		String ret = " - ";
		if (mode == INCLUDE) {
			ret = "include ";
		} else if (mode == EXCLUDE) {
			ret = "exclude";
		} else if (mode == RESTRICT) {
			ret = "restrict to";
		}
		ret += " points where ";
		ret += condition;
		return ret;
	}


	public void setInclude() {
		mode = INCLUDE;
	}

	public void setExclude() {
		mode = EXCLUDE;
	}

	public void setRestrict() {
		mode = RESTRICT;
	}

	public void setCondition(String cond) {
		condition = cond;

		regionMode = TreeMatcher.NOREGION;

		String c = cond.replaceAll(" ", "");
		if (c.startsWith("region=")) {
			region = c.replace("region=", "");
			regionMode = TreeMatcher.WHERE;

		} else if (c.startsWith("region<")) {
			region = c.replace("region<", "");
			regionMode = TreeMatcher.PROXIMAL;

		} else if (c.startsWith("region>")) {
			region = c.replace("region>", "");
			regionMode = TreeMatcher.DISTAL;

		} else {
		//	E.info("no region in " + cond);
			region = null;
		}

	}


	public boolean isInclude() {
		 return (mode == INCLUDE);
	}

	public boolean isExclude() {
		return (mode == EXCLUDE);
	}

	public boolean isRestrict() {
		return (mode == RESTRICT);
	}

	public String getCondition() {
		return condition;
	}


	public boolean isRegion() {
		return (region != null);
	}

	public String getRegion() {
		return region;
	}


	public int getRegionMode() {
		return regionMode;
	}

	public boolean isRegionDistal() {
		return (regionMode == TreeMatcher.DISTAL);
	}

	public boolean isRegionProximal() {
		return (regionMode == TreeMatcher.PROXIMAL);
	}

	public boolean isRegionWhere() {
		return (regionMode == TreeMatcher.WHERE);
	}


	public boolean dependsOn(String lbl) {
		boolean ret = false;
		if (region != null && region.equals(lbl)) {
			ret = true;
		}
		return ret;
	}

}
