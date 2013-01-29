package org.psics.num.model.synapse;

import org.psics.be.DifferentiableFunction;
import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.num.math.NewtonRaphson;
import org.psics.quantity.phys.Conductance;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.util.TextDataWriter;


// N.B. the differentiable function implemented is the *gradeint* of the timecourse
// it is for NewtonRaphson to find the point at which the gradient
// is zero.
public class TableSynapse implements DifferentiableFunction {

	String id;
	
	double erev;
	double gBase;
	
	int nid;
	
	double trise = 0.;
	int ndec = 0; 
	double[] tdecs = new double[4];
	double[] fdecs = new double[4];
	
	double fnorm = 1.;

	
	public TableSynapse(String s) {
		id = s;
	}
	
	public String getID() {
		return id;
	}
	
	public Integer getNID() {
		return nid;
	}

	public void setNID(int id) {
		nid = id;
	}

	public void appendTo(TextDataWriter tdw, double rtemp) {
		tdw.add(id);
		int[] ii = new int[3];
		ii[0] = nid;
		ii[1] = ndec;
		if (trise > 0) {
			ii[2] = 1;
		} else {
			ii[2] = 0;
		}
		tdw.addInts(ii);
		tdw.addMeta("table index, n decays, n rises(0 or 1)");
		
		int nval = 3 + 2 * ii[1] + ii[2];
		double[] vv = new double[nval];
		vv[0] = fnorm;
		vv[1] = gBase;
		vv[2] = erev;
		for (int i = 0; i < ndec; i++) {
			vv[3 + 2 * i] = fdecs[i];
			vv[3 + 2 * i + 1] = tdecs[i];
		}
		if (trise > 0) {
			vv[vv.length - 1] = trise;
		}
		tdw.add(vv);
		tdw.addMeta("normalization, gBase, erev, (f, tau)*ndecays, rise tau (if any)");
	}

	
	public void setDefaultReversal(Voltage v) {
		erev = CalcUnits.getVoltageValue(v);
	}


	public void setBaseConductance(Conductance c) {
		gBase = CalcUnits.getConductanceValue(c);
	}


	public SynapseSet makeSynapseSet(int nchan) {
		SynapseSet ret = null;

	 
		ret = new DefaultSynapseSet(this, nchan, erev, gBase);
		 

		return ret;
	}

	public void addDecay(Time tau, double f) {
		 fdecs[ndec] = f;
		 tdecs[ndec] = CalcUnits.getTimeValue(tau);
		 ndec += 1;
	}
	
	public void setRise(Time tr) {
		trise = CalcUnits.getTimeValue(tr);
	}
	
	public void normalize() {
		double ftot = 0.;
		for (int i = 0; i < ndec; i++) {
			ftot += fdecs[i];
		}
		for (int i = 0; i < ndec; i++) {
			fdecs[i] /= ftot;
		}
		if (trise <= 0) {
			// all fine.
			
		} else {
			NewtonRaphson nr = new NewtonRaphson(this);
			double tmin = nr.getRoot(trise, 1.e-6);
			double fmin = valueAt(tmin);
			fnorm = 1. / fmin;
		}
	}
	
	
	public double valueAt(double t) {
		double v = 0.;
		for (int i = 0; i < ndec; i++) {
			v += fdecs[i] * Math.exp(-1. * t / tdecs[i]);
		}
		v -= Math.exp(-1. * t / trise);
		return v;
	}
	
	
	// NB this returns the value of the gradient
	public double getValue(double t) {
		double v = 0.;
		for (int i = 0; i < ndec; i++) {
			v -= (fdecs[i] / tdecs[i]) * Math.exp(-1. * t / tdecs[i]);
		}
		v += (1. / trise) * Math.exp(-1. * t / trise);
		return v;
	}


	// NB, and this is the gradient of the gradient
	public double getGradient(double t) {
		double v = 0.;
		for (int i = 0; i < ndec; i++) {
			v += (fdecs[i] / (tdecs[i] * tdecs[i])) * Math.exp(-1. * t / tdecs[i]);
		}
		v -= (1. / (trise * trise)) * Math.exp(-1. * t / trise);
		return v;
	}
	
}
