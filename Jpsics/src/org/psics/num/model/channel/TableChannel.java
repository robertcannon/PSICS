package org.psics.num.model.channel;

import org.psics.be.E;
import org.psics.fort.FormattedDataException;
import org.psics.fort.LineDataReader;
import org.psics.num.CalcUnits;
import org.psics.num.ChannelGE;
import org.psics.num.math.Matrix;
import org.psics.num.math.Random;
import org.psics.quantity.phys.Conductance;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;
import org.psics.util.TextDataWriter;


public class TableChannel {

	String id;
	int nid;

	GCTable[] complexes;
	double vMin;
	double deltaV;
	int nV;
	double deltaT;
	Matrix[][] mata;
	double erev;
	double gBase;
	int stochasticThreshold = 0;

	boolean gated;

	double temperature;

	TableChannel alt; // alternative form of same channel (multi-complex / single complex)


	public TableChannel(String s) {
		id = s;
	}

	public void setID(String s) {
		id = s;
	}

	public void setNID(int n) {
		nid = n;
	}

	public int getNID() {
		return nid;
	}

	public String getID() {
		return id;
	}


	public boolean isGated() {
		return gated;
	}


	public void setNonGated() {
		gated = false;
	}


	public int nComplex() {
		return (complexes != null ? complexes.length : 0);
	}


	public void allocateGatingComplexes(int n) {
		complexes = new GCTable[n];
		gated = true;

	}


	public void setGatingComplex(int i, GCTable gctbl) {
		complexes[i] = gctbl;
	}


	public void setDefaultReversal(Voltage v) {
		erev = CalcUnits.getVoltageValue(v);
	}


	public void setBaseConductance(Conductance c) {
		gBase = CalcUnits.getConductanceValue(c);
	}



	// write a fortran style save file
	public String serializeFormatted() {
		StringBuffer sb = new StringBuffer();
		sb.append("TableChannel " + id + "\n");
		sb.append("" + complexes.length + "   // the number of gating compexes");
		for (int i = 0; i < complexes.length; i++) {
			sb.append(complexes[i].serializeFormatted());
		}
		return sb.toString();
	}


	public void deserializeFormatted(LineDataReader fl) throws FormattedDataException {
		String[] sa = fl.readStrings(2);
		if (sa[0] != "TableChannel") {
			throw new FormattedDataException("expecting TableChannel but got " + sa[0]);
		}
		id = sa[1];
		int ncomp = fl.readInt();
		complexes = new GCTable[ncomp];
		for (int i = 0; i < ncomp; i++) {
			GCTable gct = new GCTable();
			gct.desearializeFormatted(fl);
		}
	}


	public void buildTransitionTables(Temperature temp, Time dt, Voltage vmin, Voltage vmax, Voltage dv) {
		vMin = CalcUnits.getVoltageValue(vmin);
		double vMax = CalcUnits.getVoltageValue(vmax);
		deltaV = CalcUnits.getVoltageValue(dv);
		nV = (int) ((vMax - vMin) / deltaV + 1);



		// E.info("tabulating from " + vMin + " to " + vMax + " with dv= " +
		//  deltaV + " in " + nV + " steps");

		deltaT = CalcUnits.getTimeValue(dt);
		temperature = CalcUnits.getTemperatureValue(temp);

		mata = new Matrix[nV][complexes.length];
 
		for (int iv = 0; iv < nV; iv++) {
			for (int ic = 0; ic < complexes.length; ic++) {
				mata[iv][ic] = complexes[ic].makeMatrix(vmin.add(dv.times(iv)), temp, dt);
			}
		}
	}


	public void printMatrix(double vmem) {
		int iv = getIV(vmem);
		double f = (vmem - (vMin + iv * deltaV)) / deltaV;
		double wf = 1. - f;

		E.info("interp between " + iv + " " + (iv+1) + " " + wf + " " + f);
		Matrix[] ma = mata[iv];


		for (int i = 0; i < ma.length; i++) {
			ma[i].dump();
			//mb[i].dump();
		}
	}



	private final int getIV(double v) {
		int iv = (int) ((v - vMin) / deltaV);
		iv = (iv < 0 ? 0 : (iv >= nV - 1 ? nV - 2 : iv));
		return iv;
	}


	void ensembleAdvance(double vmem, double[][] reloc) {

		int iv = getIV(vmem);
		double f = (vmem - (vMin + iv * deltaV)) / deltaV;
		double wf = 1. - f;

		Matrix[] ma = mata[iv];
		Matrix[] mb = mata[iv + 1];

		int ngc = ma.length;

		for (int igc = 0; igc < ngc; igc++) {
			double[] rr = reloc[igc];
			double[] ga = ma[igc].rvprod(rr);
			double[] gb = mb[igc].rvprod(rr);
			for (int i = 0; i < ga.length; i++) {
				rr[i] = f * gb[i] + wf * ga[i];
			}
		}
	}


	public String numinfo(double vmem) {
		int iv = getIV(vmem);
		double f = (vmem - (vMin + iv * deltaV)) / deltaV;
		String ret = " iv=" + iv + " f=" + f + mata[iv].toString();
		return ret;
	}



	public double ensembleGeff(double[][] reloc) {
		double fopen = 1.;
		for (int igc = 0; igc < complexes.length; igc++) {
			GCTable gct = complexes[igc];
			fopen *= Math.pow(gct.fOpen(reloc[igc]), gct.nInstances);
		}
		return fopen;
	}


	void stochasticAdvance(double vmem, int[][] istoch) {

		int iv = getIV(vmem);

		double f = (vmem - (vMin + iv * deltaV)) / deltaV;
		double wf = 1. - f;

		Matrix[] ma = mata[iv];
		Matrix[] mb = mata[iv + 1];


		int ngc = ma.length;
		int nchan = istoch.length;

		// E.info("stoch adv using matrix " + iv + " " + vmem + " " + nchan);

		for (int ich = 0; ich < nchan; ich++) {
			int[] ist = istoch[ich];

			for (int igc = 0; igc < ngc; igc++) {
				double[] ca = ma[igc].getColumn(ist[igc]);
				double[] cb = mb[igc].getColumn(ist[igc]);

				double rv = Random.uniformRV();

				int ir = 0;
				while ((rv -= (f * cb[ir] + wf * ca[ir])) > 0) {
					ir++;
				}
				ist[igc] = ir;
			}
		}
	}


	public double stochasticGeff(int[] state) {
		double fopen = 1.;
		for (int igc = 0; igc < complexes.length; igc++) {
			fopen *= complexes[igc].relativeConductance(state[igc]);
		}
		return fopen;
	}


	public void setStochThreshold(int i) {
		stochasticThreshold = i;
	}


	public ChannelSet makeChannelSet(int nchan) {
		ChannelSet ret = null;

		if (gated) {

			if (nchan > stochasticThreshold) {
 			    ret = new EnsembleChannelSet(this, nchan);
			} else {
				ret = new StochasticChannelSet(this, nchan);
			//	E.info("made scs " + nchan + " " + getID());
			}

		} else {
			ret = new NonGatedChannelSet(this, nchan, erev, gBase);
		}

		return ret;
	}


	public double[][] equlibriumOccupancy(double v) {
		int iv = getIV(v);
		double f = (v - (vMin + iv * deltaV)) / deltaV;
		double wf = 1. - f;

		Matrix[] ma = mata[iv];
		Matrix[] mb = mata[iv + 1];

		double[][] ret = new double[ma.length][];
		for (int i = 0; i < ma.length; i++) {
			double[] ea = ma[i].ev1vec(16);
			double[] eb = mb[i].ev1vec(16);

			ret[i] = new double[ea.length];
			for (int j = 0; j < ea.length; j++) {
				ret[i][j] = f * eb[j] + wf * ea[j];
			}
		}
		return ret;
	}



	public double[][] singleEqulibriumOccupancy(Voltage v, Temperature temp) {
		double[][] ret = new double[complexes.length][];
		for (int ic = 0; ic < complexes.length; ic++) {
			Matrix m = complexes[ic].makeMatrix(v, temp, new Time(0.1, Units.ms));
			ret[ic] = m.ev1vec(24);
		}
		return ret;
	}


	public double[][] singleEqulibriumOccupancyTable(double v) {
		double[][] ret = new double[complexes.length][];


		int iv = getIV(v);

		double f = (v - (vMin + iv * deltaV)) / deltaV;
		double wf = 1. - f;

		Matrix[] ma = mata[iv];
		Matrix[] mb = mata[iv + 1];


		for (int ic = 0; ic < ma.length; ic++) {
			double[] ra = ma[ic].ev1vec(16);
			double[] rb = mb[ic].ev1vec(16);
			ret[ic] = new double[ra.length];
			for (int i = 0; i < ra.length; i++) {
				ret[ic][i] = f * rb[i] + wf * ra[i];
			}
		}
		return ret;
	}



	public void appendTo(TextDataWriter tdw, double temp) {
		tdw.add(id);
		int nc = 0;
		if (complexes != null) {
			nc = complexes.length;
		}
		int altid = -1;
		if (alt != null) {
			altid = alt.getNID();
		}
		tdw.addInts(nid, stochasticThreshold, nV, nc, altid);
		tdw.addMeta("numeric id, stochastic thresold, nV, n complex, alt form id");

		tdw.add(gBase, erev, vMin, deltaV);
		tdw.addMeta("gBase, erev, vMin, deltaV");


		if (nc > 0) {
			double[] vs = new double[nV];
			for (int i = 0; i < nV; i++) {
				  vs[i] = (vMin + i * deltaV);
			}
			for (int i = 0; i < nc; i++) {
			complexes[i].appendTo(tdw, vs, temp);
		}
	}

	}

	public void setAlt(TableChannel tchmc) {
		alt = tchmc;
	}


	public TableChannel getAlt() {
		return alt;
	}


	public ChannelGE getChannelGE(Voltage potential) {
		 return getChannelGE(potential, new Temperature(temperature, Units.K));
	}


	public ChannelGE getChannelGE(Voltage potential, Temperature temp) {
		double geff = gBase;
		if (complexes != null && complexes.length > 0) {
			double[][] veq = singleEqulibriumOccupancy(potential, temp);
			geff *= ensembleGeff(veq);
		}
		ChannelGE ret = new ChannelGE(geff, erev);
		return ret;
	}

	public ChannelGE getChannelTableGE(Voltage potential) {
		double geff = gBase;
		double v = CalcUnits.getVoltageValue(potential);
		if (complexes != null && complexes.length > 0) {
			double[][] veq = singleEqulibriumOccupancyTable(v);
			geff *= ensembleGeff(veq);
		} else {
			E.error("no complexes?");
		}
		ChannelGE ret = new ChannelGE(geff, erev);
		return ret;
	}



}
