package org.psics.distrib;

import org.psics.be.E;
import org.psics.be.RandomNumberGenerator;
import org.psics.geom.GVector;
import org.psics.geom.Geom;
import org.psics.geom.Rotation;
import org.psics.geom.Vector;
import org.psics.num.Compartment;
import org.psics.num.CompartmentTree;
import org.psics.num.math.Array;
import org.psics.num.math.MersenneTwister;


public class PointPopulation {

	float[][][] chpos;


	// following for compartment trees - not needed now?
	int nch;
	double[][] xch;
	double[][] ych;
	double[] rnchan;

	CompartmentTree bufCtree = null;
	PointTree bufChTree = null;

	DistribPopulation population;

	boolean show;
	boolean synced3 = false;
	boolean positionsReady = false;


	public PointPopulation(DistribPopulation p) {
		population = p;
	}


	public String getTypeID() {
		return population.getTypeID();
	}


	public boolean hasPositions() {
		return positionsReady;
	}


	public Object getColor() {
		return population.getBestColor();
	}


	public void labelMoved(String lbl) {
		if (population.dependsOnLabel(lbl)) {
			population.flagChange();
		}
	}


	public boolean needsRemake(PointTree chTree) {
		boolean ret = false;
		if (chpos == null || population.hasChanged() || chTree != bufChTree) {
			ret = true;
			synced3 = false;
		}
		return ret;
	}


	public float[][][] getCHPos() {
		return chpos;
	}


	public void setSynced3() {
		synced3 = true;
	}


	public void setUnsynced3() {
		synced3 = false;
	}


	public boolean isSynced3D() {
		return synced3;
	}


	public void realize(PointTree chTree, RandomNumberGenerator rng, boolean squareCaps) {
		// TODO check if needed (if pop has changed)
		float[][] chwk = new float[10000][8];

		if (needsRemake(chTree)) {
			int nchtot = 0;
			bufChTree = chTree;
			chTree.subdivide(1., squareCaps); // TODO - parameterize?
			population.unflagChanged();
			double[][] dens = population.getDensities(chTree);
			double[][] sas = chTree.getSurfaceAreas();
			double[][][] epos = chTree.getDivisionEndPositions();
			double[][] rads = chTree.getDivisionEndRadii();
			double[][] plengths = chTree.getDivPathLength();
			boolean[] isterms = chTree.getIsTerminal();
/*
			E.dump("sas 0", sas[0]);
			E.dump("sas 1", sas[1]);
			E.dump("rads 0", rads[0]);
			E.dump("rads 1", rads[1]);
			E.dump("rads 2", rads[2]);
	*/

			long seed = population.getSeed();
			if (seed > 0) {
				rng.setSeed(seed);
			}

			double ftot = 1.;

			double dsa = 0.;
			for (int i = 0; i < dens.length; i++) {
				double a = 0.;
				for (int j = 0; j < sas[i].length; j++) {
					a += sas[i][j] * dens[i][j];
				}
				dsa += a;
			}

			if (population.getFixTotal()) {
				if (dsa > 0.) {
					ftot = population.getTotalNumber() / dsa;
				} else {
					E.error("population has fixed total but density is zero everywhere");
				}
			}

			chpos = new float[dens.length][][];

			double frem = 0.;

			for (int i = 0; i < dens.length; i++) {
				double[] asas = sas[i];
				double[] aden = dens[i];
				double[][] aepos = epos[i];
				double[] arad = rads[i];
				double[] apl = plengths[i];
				// double plend = chTree.getPathLength(i);
				int nchseg = 0;

				double dlj = 1. / asas.length;
				for (int j = 0; j < asas.length; j++) {
					if (aden[j] > 0.) {
						// TODO not ideal - frem should go on neighbors

						int nn = 0;
						double[] fn = null;
						double[] thetan = null;


						if (population.isRegular()) {
							double rn = ftot * asas[j] * aden[j] + frem;
							nn = (int) (Math.round(rn));
							if (nn < 0) {
								nn = 0;
							}

							fn = new double[nn];
							thetan = new double[nn];
							for (int k = 0; k < nn; k++) {
								fn[k] = (k + 0.5) / nn;
								thetan[k] = Math.PI * (2 * rng.random() - 1);
								// thetan[k] = theta;
								// theta += 0.1;
							}
							frem = rn - nn;

						} else {
							double rn = ftot * asas[j] * aden[j];
							int ntry = (int) (10 * rn); // TODO 10? perf,
							// smaller?
							double p = rn / ntry;
							int nlm = 2 * (int) rn + 10;
							fn = new double[nlm];
							thetan = new double[nlm];
							nn = 0;
							for (int k = 0; k < ntry; k++) {
								if (rng.random() < p) {
									fn[nn] = (k + 0.5) / ntry;
									thetan[nn] = 2. * Math.PI * rng.random();
									nn += 1;
								}
							}
						}


						if (nn > 0) {
							// double dpl = apl[j+1] - apl[j];
							double dx = aepos[j + 1][0] - aepos[j][0];
							double dy = aepos[j + 1][1] - aepos[j][1];
							double dz = aepos[j + 1][2] - aepos[j][2];

							Rotation rot = Geom.fromZRotation(new GVector(dx, dy, dz));

							double pla = apl[j];
							double dpl = apl[j + 1] - apl[j];

							double dr = arad[j+1] - arad[j];
							double sl = Math.sqrt(dr*dr + dpl*dpl);
							double fpar = -dr / sl;
							double fperp = dpl / sl;

							for (int k = 0; k < nn; k++) {
								double f = fn[k];

								float cx = (float) (aepos[j][0] + f * dx);
								float cy = (float) (aepos[j][1] + f * dy);
								float cz = (float) (aepos[j][2] + f * dz);

								double r = f * arad[j + 1] + (1 - f) * arad[j];

								double th = thetan[k];
								double bx = Math.cos(th);
								double by = Math.sin(th);

								Vector vr = rot.getRotatedVector(new GVector(bx, by, 0));


								Vector vn = rot.getRotatedVector(new GVector(fperp * bx, fperp * by, fpar));


								float vx = (float) vr.getDX();
								float vy = (float) vr.getDY();
								float vz = (float) vr.getDZ();

								chwk[nchseg][0] = (float) (cx + r * vx);
								chwk[nchseg][1] = (float) (cy + r * vy);
								chwk[nchseg][2] = (float) (cz + r * vz);
								chwk[nchseg][3] = (float) (pla + f * dpl);
								// [4] is fractional position within original
								// parent segment used
								// for computing whether a particular point is
								// above or below the
								// midplane in a given projection
								chwk[nchseg][4] = (float) ((j + f) * dlj);
								chwk[nchseg][5] = (float)(vn.getDX());
								chwk[nchseg][6] = (float)(vn.getDY());
								chwk[nchseg][7] = (float)(vn.getDZ());

								// TODO ? any use for finer grained path
								// lengths? (f * apl[j+1] + (1 - f) * apl[j]);
								// - just needed to map to compartmentalization;
								nchseg += 1;
								if (nchseg == chwk.length) {
									chwk = enlarge(chwk);
								}
							}
						}
					}
				}

				if (isterms[i] && !squareCaps) {
					// also need some on the tip;
					double r = arad[arad.length - 1];
					double[] ep = aepos[aepos.length - 1];

					double cx = ep[0];
					double cy = ep[1];
					double cz = ep[2];

					int nend = 0;
					Rotation rot = null;
					double area = (4. * Math.PI * r * r);


					boolean half = true;

					if (aepos.length > 1) {
						double[] pep = aepos[aepos.length - 2];
						Vector vax = Geom.fromToVector(pep, ep);
						double rn = ftot * 0.5 * area * aden[aden.length - 1] + frem;
						nend = (int) (Math.round(rn));
						frem = rn - nend;
						rot = Geom.fromZRotation(vax);

					} else {
						half = false;
						double rn = ftot * area * aden[aden.length - 1] + frem;
						nend = (int) (Math.round(rn));
						frem = rn - nend;
					}


					for (int k = 0; k < nend; k++) {
						double az = 0.;
						if (half) {
							az = rng.random();
						} else {
							az = -1. + 2 * rng.random();
						}

						double ax = -1. + 2. * rng.random();
						double ay = -1. + 2. * rng.random();
						double rxy = Math.sqrt(1. - az * az);
						double f = rxy / Math.sqrt(ax * ax + ay * ay);
						ax *= f;
						ay *= f;

						double vx = 0.;
						double vy = 0.;
						double vz = 0.;

						if (half) {
							Vector v = rot.getRotatedVector(Geom.vector(ax, ay, az));
							vx = v.getDX();
							vy = v.getDY();
							vz = v.getDZ();
						} else {
							vx = ax;
							vy = ay;
							vz = az;
						}


						chwk[nchseg][0] = (float) (cx + r * vx);
						chwk[nchseg][1] = (float) (cy + r * vy);
						chwk[nchseg][2] = (float) (cz + r * vz);
						chwk[nchseg][3] = (float) (apl[apl.length - 1]);
						// [4] is fractional position within original parent
						// segment used
						// for computing whether a particular point is
						// above or below the
						// midplane in a given projection
						chwk[nchseg][4] = 1.f;
						chwk[nchseg][5] = (float) vx;
						chwk[nchseg][6] = (float) vy;
						chwk[nchseg][7] = (float) vz;

						// TODO ? any use for finer grained path lengths? (f
						// * apl[j+1] + (1 - f) * apl[j]);
						// - just needed to map to compartmentalization;
						nchseg += 1;
						if (nchseg == chwk.length) {
							chwk = enlarge(chwk);
						}
					}


				}

				if (nchseg > 0) {
					nchtot += nchseg;

					float[][] wk = new float[nchseg][8];
					for (int ich = 0; ich < nchseg; ich++) {
						for (int ic = 0; ic < 8; ic++) {
							wk[ich][ic] = chwk[ich][ic];
						}
					}
					chpos[i] = wk;
				}
			}
			nch = nchtot;

		}
		positionsReady = true;
	}


	private float[][] enlarge(float[][] src) {
		int n = src.length;
		int q = src[0].length;
		int nn = (int) (1.5 * n);
		float[][] ret = new float[nn][q];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < q; j++) {
				ret[i][j] = src[i][j];
			}
		}
		return ret;
	}


	public void realize(CompartmentTree ctree, MersenneTwister mersenne) {
		// TODO check if needed (if pop has changed)
		if (xch == null || population.hasChanged() || ctree != bufCtree) {
			bufCtree = ctree;
			population.unflagChanged();
			rnchan = population.getRNChans(ctree);

			nch = (int) (Array.sum(rnchan));
			int np = rnchan.length;
			xch = new double[np][];
			ych = new double[np][];
			int icpt = 0;

			for (Compartment cpt : ctree.getCompartments()) {
				int n = (int) (rnchan[icpt]);
				if (mersenne.random() < (rnchan[icpt] - n)) {
					n += 1;
				}
				xch[icpt] = new double[n];
				ych[icpt] = new double[n];
				for (int j = 0; j < n; j++) {
					double[] xy = cpt.randomInternalPoint(mersenne.random(), mersenne.random());
					xch[icpt][j] = xy[0];
					ych[icpt][j] = xy[1];
				}
				icpt += 1;
			}

		}
	}


	public int getNChannel() {
		return nch;
	}


	public boolean matchesPopulation(DistribPopulation cp) {
		return (population == cp);
	}


	public void setShow(boolean b) {
		show = b;
	}


	public boolean shouldShow() {
		return show;
	}


	public String getID() {
		return getPopulation().getID();
	}


	public DistribPopulation getPopulation() {
		return population;
	}


	public float[][][] getPositions() {
		return chpos;
	}


	public Object getBestColor() {
		return population.getBestColor();
	}


}
