package org.psics.morph.unused;
 

public class TreeMerger {
 
/*
	public static TreePoint[] merge(TreePoint[] pt, double maxdr,
			double maxlen, double tol) {

		for (int i = 0; i < pt.length; i++) {
			pt[i].setWork(2);
		}

		TreePoint p0 = pt[0];
		recMerge(p0, maxdr, maxlen, tol);

		// remove dead points.
		int nl = 0;
		for (int i = 0; i < pt.length; i++) {
			if (pt[i].getWork() > 0) {
				nl++;
			}
		}
		TreePoint[] pr = p0.newPointArray(nl);
		nl = 0;
		for (int i = 0; i < pt.length; i++) {
			if (pt[i].getWork() > 0) {
				pr[nl++] = pt[i];
			}
		}
		return pr;
	}
*/
	
	
	/*
	@SuppressWarnings("unchecked")
	public static void recMerge(TreePoint cp, double maxdr, double maxlen,
			double tol) {
		// may have already been done
		cp.setWork(1);

		for (TreePoint cq : cp.getNeighbors()) {

			double rp = cp.getR();
			double rq = cq.getR();

			if (cq.getWork() == 2 && cq.getNeighborCount() == 2
					&& Math.abs((rq - rp) / (rq + rp)) < 0.5 * maxdr
					&& distanceBetween(cp, cq) < maxlen) {

				// if nnbr is not two, either it is a terminal, or a branch
				// point:
				// in either case there is nothing to do;

				TreePoint cprev = cp;
				ArrayList vpt = new ArrayList();
				double ltot = 0.;
				double ldtot = 0.;

				while (cq.getNeighborCount() == 2
						&& distanceBetween(cprev, cq) < maxlen
						&& Math.abs((rq - rp) / (rq + rp)) < 0.5 * maxdr) {
					vpt.add(cq);
					double dl = distanceBetween(cprev, cq);
					ltot += dl;
					ldtot += dl * (cprev.getR() + cq.getR());

					TreePoint cnxt;
					ArrayList<TreePoint> acqn = cq.getNeighbors();
					if (acqn.get(0) == cprev) {
						cnxt = acqn.get(1);
					} else {
						cnxt = acqn.get(0);
					}

					cprev = cq;
					cq = cnxt;
					rq = cq.getR();
				}

				double dl = distanceBetween(cprev, cq);
				ltot += dl;
				ldtot += dl * (cprev.getR() + cq.getR());

				double lab = distanceBetween(cp, cq);
				double ldab = lab * (cp.getR() + cq.getR());

				int nadd = (int) (ltot / maxlen);

				if (nadd > vpt.size()) {
					nadd = vpt.size(); // cant happen at present;
				}
				// recycle nadd points;

				boolean cor = (Math.abs((lab - ltot) / (lab + ltot)) > 0.5 * tol || Math
						.abs((ldab - ldtot) / (ldab + ldtot)) > 0.5 * tol);
				if (cor && nadd == 0) {
					nadd = 1;
				}

				if (nadd == 0) {
					cp.replaceNeighbor((TreePoint) vpt.get(0), cq);
					cq.replaceNeighbor((TreePoint) vpt.get(vpt.size() - 1), cp);

				} else {

					for (int i = 0; i < nadd; i++) {
						TreePoint cm = (TreePoint) vpt.get(i);
						cm.setWork(1);
						locateBetween(cp, cq, (1. + i) / (1. + nadd), cm);
						if (i == nadd - 1 && nadd < vpt.size()) {
							cm.replaceNeighbor((TreePoint) vpt.get(nadd), cq);
							cq.replaceNeighbor((TreePoint) vpt
									.get(vpt.size() - 1), cm);
						}
					}
				}

				// just kill the rest;
				for (int jd = nadd; jd < vpt.size(); jd++) {
					TreePoint cd = (TreePoint) vpt.get(jd);
					cd.disconnect();
					cd.setWork(0);
				}

				if (cor) {
					double dpar = lab / (nadd + 1);
					double fl = ltot / lab;
					double dperp = Math.sqrt((fl * fl - 1) * dpar * dpar);
					double fr = ldtot / (fl * ldab);
					for (int i = 0; i < nadd; i++) {
						TreePoint cm = (TreePoint) (vpt.get(i));

						double cmr = cm.getR();
						cm.setR(cmr * fr);

						if (i % 2 == 0) {
							movePerp(cp, cq,
									((i / 2) % 2 == 0 ? dperp : -dperp), cm);
						}
					}

				}

			}
			if (cq.getWork() == 2) {
				recMerge(cq, maxdr, maxlen, tol);
			}
		}
	}
*/
}
