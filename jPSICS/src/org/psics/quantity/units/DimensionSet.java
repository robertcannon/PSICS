package org.psics.quantity.units;

import java.util.StringTokenizer;

import org.psics.be.E;

public class DimensionSet {

	private int[] mltak = new int[5];

	private int pTEN;

	private boolean pure;
	private double fac;

	private String name;
	private String check;

	public DimensionSet() {
		name = "";
		check = "";
		pure = true;
		pTEN = 0;
		fac = 0.;
	}

	public DimensionSet(int m, int l, int t, int a, int k,    int qTen, double ff, String sn, String sc) {
		mltak[0] = m;
		mltak[1] = l;
		mltak[2] = t;
		mltak[3] = a;
		mltak[4] = k;

		pTEN = qTen;
		fac = 0.;
		if (ff != 0.) {
			fac = ff;
			pure = false;
		} else {
			pure = true;
		}

		name = sn;
		check = sc;
	}


	public String toString() {
		StringBuffer ret = new StringBuffer();
		ret.append("Dims:" + name + "(");
		for (int i = 0; i < mltak.length; i++) {
			ret.append("" + mltak[i] +",");
		}
		ret.append(" " + pTEN  + "; ");
		ret.append(pure ? " pure)" : "f=" + fac + ")");
		return ret.toString();
	}


	public String getName() {
		return name;
	}


	public boolean isPure() {
		return pure;
	}

	public double getFac() {
		return fac;
	}

	public int getPTen() {
		return pTEN;
	}

	public int[] getMLTAK() {
		return mltak;
	}




	public String getCheckString() {
		return check;
	}


	public DimensionSet copy() {
		DimensionSet ret = new DimensionSet(mltak[0], mltak[1], mltak[2], mltak[3], mltak[4],
					pTEN, (pure ? 0. : fac), name, check);
		return ret;
	}




	public static DimensionSet find(String lbl) {
		DimensionSet ret = null;

		Units us = Units.getByLabel(lbl);
		if (us != null) {
			ret = us.getDimensionSet();
		}

		if (ret == null) {
			try {
				double d = Double.parseDouble(lbl);
				ret = new DimensionSet();
				if (d == 0.) {
					ret.pure = true;

				} else {
					double ltd = Math.log10(d);
					if (doublesMatch(d, Math.pow(10., Math.round(ltd)))) {
						ret.pure = true;
						ret.pTEN = (int)Math.round(ltd);
					} else {
						ret.pure = false;
						ret.pTEN = (int)(Math.round(Math.floor(ltd)));
						ret.fac = d / Math.pow(10., ret.pTEN);
					}
				}
			} catch (Exception ex) {
				ret = null;
			}
		}
		return ret;
	}






	public boolean matches(DimensionSet d) {
		boolean ret = true;
		for (int i = 0; i < mltak.length; i++) {
			if (mltak[i] != d.mltak[i]) {
				ret = false;
			}
		}
		if (pure && d.pure && (pTEN != d.pTEN)) {
			ret = false;
		} else {
			if (!doublesMatch(fac * Math.pow(10, pTEN), Math.pow(10, d.pTEN) * d.fac)) {
				ret = false;
			}
		}
		return ret;
	}


	private static boolean doublesMatch(double a, double b) {
		boolean ret = true;
		if (a == 0 && b == 0) {
			// ok;
		} else if (a != 0 && b != 0) {
			if (Math.abs((a - b) / (a + b)) < 1.e-7) {
				ret = true;
			} else {
				// one zero the other not is never a match;
				ret = false;
			}
		} else {
			ret = false;
		}
		return ret;
	}




	public DimensionSet times(DimensionSet d) {
		DimensionSet ret =  psum(d, 1);
		ret.name = "(" + name + " times " + d.name + ")";
		return ret;
	}

	public DimensionSet dividedBy(DimensionSet d) {
		DimensionSet ret = psum(d, -1);
		ret.name = "(" + name + " over " + d.name + ")";
		return ret;
	}





	private DimensionSet psum(DimensionSet d, int n) {
		DimensionSet ret = copy();
		for (int i = 0; i < mltak.length; i++) {
			ret.mltak[i]  = mltak[i] + n * d.mltak[i];
		}
		ret.pTEN  = pTEN + n * d.pTEN;
		ret.check = "";

		if (pure && d.pure) {
			ret.pure = true;
		} else {
			ret.pure = false;
			ret.fac = (pure ? 1. : fac) *  Math.pow((d.pure ? 1. : d.fac), n);
		}
		return ret;
	}



	public void checkRelations() {
		if (check.trim().length() > 0) {
			StringTokenizer st = new StringTokenizer(check, ",");
			while (st.hasMoreTokens()) {
				checkOneRelation(st.nextToken());
			}
		}
	}



	private void checkOneRelation(String expr) {
			StringTokenizer st = new StringTokenizer(expr, " ");
			if (st.countTokens() == 3) {
				checkRel(st.nextToken(), st.nextToken(), st.nextToken());

			} else {
				E.warning("cant check relation " + check);
			}
	}



	private void checkRel(String a, String op, String b) {
			int comb = 0;

			if (op.equals("/")) {
				comb = -1;

			} else if (op.equals("*")) {
				comb = +1;
			} else {
				E.warning("unrecognized operator " + op + " checking " + check);
				return;
			}

			DimensionSet da = find(a);
			DimensionSet db = find(b);


			if (da != null && db != null) {



				DimensionSet dc = null;
				if (comb == 1) {
					dc = da.times(db);
				} else {
					dc = da.dividedBy(db);
				}
				if (matches(dc)) {
					E.info("check passed for " + name + " (" + check + ")");
				} else {
					E.warning("check FAILED for " + name + " (" + check + ")\n " +
					 "          when comparing " + this + "  with " + dc + "\n" +
					 " 			from op " + comb + " of " + da + " and " + db);
				}
			} else {
				E.error("cant get units " + a + " or " + b);
			}
		}





	public boolean sameDimensionsAs(Units u) {
		DimensionSet d = u.getDimensionSet();
		return sameDimensionsAs(d);
	}

	public boolean sameDimensionsAs(DimensionSet d) {
		boolean ret = true;
		for (int i = 0; i < mltak.length; i++) {
			if (mltak[i] != d.mltak[i]) {
				ret = false;
			}
		}
		return ret;
	}



	public double getToConversionFactor(DimensionSet dset) {
		 double f = 0.;
		 if (sameDimensionsAs(dset)) {
			 if (pure && dset.pure) {
				 f = Math.pow(10, pTEN - dset.pTEN);

			 } else {
				 double fme = (pure ? fac : 1.) * Math.pow(10, pTEN);
				 double ftgt = (dset.pure ? dset.fac : 1.) * Math.pow(10, dset.pTEN);
				 f = fme / ftgt;
			 }

		 } else {
			 E.error("non-convertible units " + this + " " + dset);
		 }
		 return f;
	}

}

