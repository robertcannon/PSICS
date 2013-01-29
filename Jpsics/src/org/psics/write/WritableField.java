package org.psics.write;

import org.psics.be.Textalizer;

public abstract class WritableField implements Comparable {

	String fieldName;
	double posn;

	Textalizer tlz;


	public WritableField(String s, double p) {
		fieldName = s;
		posn = p;
	}

	public abstract String write(ListPosition lp);


	public void setTextalizer(Textalizer t) {
		tlz = t;
	}

	public String capitalize(String s) {
		String ret = s.substring(0,1).toUpperCase() + s.substring(1, s.length());
		return ret;
	}



	public double getPos() {
		return posn;
	}


	public int compareTo(Object o) {
		int ret = -1;
		if (o instanceof WritableField) {
			double pme = getPos();
			double pit = ((WritableField)o).getPos();
			if (pme < pit) {
				ret = -1;
			} else if (pme > pit) {
				ret = 1;
			} else {
				ret = 0;
			}
		}
		return ret;
	}
}
