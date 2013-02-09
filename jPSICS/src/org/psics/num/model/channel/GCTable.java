package org.psics.num.model.channel;

import org.psics.be.E;
import org.psics.be.TransitionEvaluator;
import org.psics.fort.FUtil;
import org.psics.fort.FormattedDataException;
import org.psics.fort.LineDataReader;
import org.psics.num.CalcUnits;
import org.psics.num.math.Matrix;

import org.psics.quantity.phys.*;
import org.psics.util.TextDataWriter;


public class GCTable {

	int nstate;
	String[] stateIDs;
	double[] conductances;   // REFAC fOpen?

	int ntrans;
	int[] transFrom;
	int[] transTo;
	int[] transType;
	double[][] transData;

	int nInstances;

	TransitionEvaluator[] evaluators;


	public GCTable() {

	}


	public void allocateStates(int n) {
		nstate = n;
		stateIDs = new String[n];
		conductances = new double[n];
	}


	public void allocateTransitions(int n) {
		ntrans = n;
		transFrom = new int[n];
		transTo = new int[n];
		transType = new int[n];
		transData = new double[n][10];
	    evaluators = new TransitionEvaluator[n];
	}


	public void setNInstances(int ni) {
		 nInstances = ni;
	}

	public void setStateData(int i, String id, double grel) {
		 stateIDs[i] = id;
		 conductances[i] = grel;

	}


	public void setTransitionData(int i, int code, int ifrom, int ito, double[] td) {
		transType[i] = code;
		transFrom[i] = ifrom;
		transTo[i] = ito;

		for (int j = 0; j < td.length; j++) {
			transData[i][j] = td[j];
			if (Double.isNaN(td[j])) {
				E.fatalError("setting NaN data for element " + j + " in GCTable");
			}
		}
	}


	public String serializeFormatted() {
		E.missing("need forward and reverse factors");
		 StringBuffer sb = new StringBuffer();
		 sb.append("" + stateIDs.length);
		 sb.append(" \\ number of states, next their IDs and conductances\n");
		 sb.append(FUtil.writeLineArray(stateIDs));
		 sb.append(FUtil.writeLineArray(conductances, "%12.5g"));
		 sb.append("" + transFrom.length);
		 sb.append(" \\ number of transitions, next type, from, to,  and data for each\n");
		 for (int i = 0; i < transFrom.length; i++) {
			 sb.append("" + transType[i] + " " + transFrom[i] + " " + transTo[i] + "\n");
			 sb.append(FUtil.writeLineArray(transData[i], "%12.5g"));
		 }
		 return sb.toString();
	}


	public void desearializeFormatted(LineDataReader fl) throws FormattedDataException {
		int nstat = fl.readInt();
		stateIDs = fl.readStrings(nstat);
		conductances = fl.readDoubles(nstat);
		int ntr = fl.readInt();
		transType = new int[ntr];
		transFrom = new int[ntr];
		transTo = new int[ntr];

		transData = new double[ntr][6];

		for (int i = 0; i < ntr; i++) {
			int[] ftt = fl.readInts(3);
			transType[i] = ftt[0];
			transFrom[i] = ftt[1];
			transTo[i] = ftt[2];

			double[] dat = fl.readDoubles(6);
			for (int j = 0; i < dat.length; j++) {
				transData[i][j] = dat[j];
			}
		}




	}


	/*
    if (!doneInit) {
        double ebyk = 1.; // XXX Phys.electronCharge / Phys.boltzmannConstant;
//XXX         ebykt = Units.from_perV(ebyk / 25.); // *********
        evalCharge();
        doneInit = true;
     }
*/



	public Matrix makeMatrix(Voltage v, Temperature tK, Time dt) {

		Matrix m = new Matrix(nstate);

		for (int itr = 0; itr < ntrans; itr++) {
			int ia = transFrom[itr];
			int ib = transTo[itr];

			double fwd = forwardRate(itr, v, tK);
			double rev = reverseRate(itr, v, tK);

			m.a[ib][ia] += fwd;
			m.a[ia][ia] -= fwd;
			m.a[ia][ib] += rev;
			m.a[ib][ib] -= rev;

		}

		m = m.expOf(CalcUnits.getTimeValue(dt));
		return m;
 	}


	@SuppressWarnings("unused")
	public void appendTransitions(TextDataWriter tdw) {
		E.missing();
	}

	public void appendRates(double v, double temp, TextDataWriter tdw) {
		tdw.addRow(v);
		for (int itr = 0; itr < ntrans; itr++) {

			double fwd = forwardRate(itr, v, temp);
			double rev = reverseRate(itr, v, temp);
			tdw.addRow(fwd, rev);
		}
			tdw.addEcoMeta("v, (fwd, rev) for each transition");
			tdw.endRow();

	}




	private double forwardRate(int itr, Voltage vv, Temperature tK) {
		double v = CalcUnits.getVoltageValue(vv);
		double temp = CalcUnits.getTemperatureValue(tK);
		return forwardRate(itr, v, temp);
	}


	private double forwardRate(int itr, double v, double temp) {

		int itt = transType[itr];
		double[] dat = transData[itr];

		double ret = Double.NaN;
		if (itt == TransitionTypes.EXP_LINEAR_ONE_WAY) {
			ret = TransitionTypes.expLinearForwardRate(v, dat);

		} else if (itt == TransitionTypes.FIXED_RATE) {
			ret = dat[2];

		} else if (itt == TransitionTypes.BOLTZMANN_VDEP) {
			ret = TransitionTypes.boltzmannForwardRate(v, dat);

		} else if (itt == TransitionTypes.EXP_ONE_WAY) {
			ret = TransitionTypes.expForwardRate(v, dat);

		} else if (itt == TransitionTypes.SIGMOID_ONE_WAY) {
					ret = TransitionTypes.sigmoidForwardRate(v, dat);

		} else if (itt == TransitionTypes.CODED) {
			double[] ab = evaluators[itr].alphaBeta(v, temp);
			ret = ab[0];

		} else if (itt == TransitionTypes.FUNCTION) {
			double[] ab = evaluators[itr].alphaBeta(v, temp);
			ret = ab[0];
		}

		if (dat[1] != 0.) {
			double tfac = Math.pow(dat[1], (temp - dat[0])/10.);
			ret *= tfac;
		}


		if (Double.isNaN(ret)) {
		  E.oneLineError("forward transition generated NaN rate " + itt);
	  	  E.dump("trans data ", transData[itr]);
		}


		return ret;

	}





	private double reverseRate(int itr, Voltage vv, Temperature tK) {
		double v = CalcUnits.getVoltageValue(vv);
		double temp = CalcUnits.getTemperatureValue(tK);
		return reverseRate(itr, v, temp);
	}


 
	private double reverseRate(int itr, double v, double temp) {
		double ret = Double.NaN;

		int itt = transType[itr];
		double[] dat = transData[itr];

		if (itt == TransitionTypes.EXP_LINEAR_ONE_WAY) {
			ret = 0.;

		} else if (itt == TransitionTypes.FIXED_RATE) {
			ret = dat[3];

		} else if (itt == TransitionTypes.BOLTZMANN_VDEP) {
			ret = TransitionTypes.boltzmannReverseRate(v, dat);


	} else if (itt == TransitionTypes.EXP_ONE_WAY) {
		ret = 0.;

	} else if (itt == TransitionTypes.SIGMOID_ONE_WAY) {
				ret = 0.;

	} else if (itt == TransitionTypes.CODED) {
		double[] ab = evaluators[itr].alphaBeta(v, temp);
		ret = ab[1];

	} else if (itt == TransitionTypes.FUNCTION) {

		double[] ab = evaluators[itr].alphaBeta(v, temp);
		ret = ab[1];
	}


		if (dat[1] != 0.) {
			double tfac = Math.pow(dat[1], (temp - dat[0])/10.);
			ret *= tfac;
		}

		if (Double.isNaN(ret)) {
			E.oneLineError("reverse transition generated NaN rate " + itt);
			E.dump("trans data ", transData[itr]);
		}


		return ret;
	}



	 double fOpen(double[] reloc) {
	     double ret = 0.;
	     for (int i = 0; i < conductances.length; i++) {
	    	 ret += reloc[i] * conductances[i];
	     }
	     return ret;
	 }


	 double relativeConductance(int istat) {
		 return conductances[istat];
	 }


	public void appendTo(TextDataWriter tdw, double[] vs, double temp) {
		int[] ift = new int[3 + 2 * ntrans];
		int nsf = 0;
		ift[nsf++] = nInstances;
		ift[nsf++] = nstate;
		ift[nsf++] = ntrans;

		for (int itr = 0; itr < ntrans; itr++) {
			ift[nsf + 2 * itr] = transFrom[itr];
			ift[nsf + 2 * itr + 1] = transTo[itr];
		}
		tdw.addInts(ift);
		tdw.addMeta("ninstances, nstate, ntransition, (from, to)*ntransition");

		tdw.add(conductances);
		for (int i = 0; i < nstate; i++) {
			tdw.addMeta(stateIDs[i] + "   ");
		}
		tdw.endRow();
	    for (int i = 0; i < vs.length; i++) {
		   appendRates(vs[i], temp, tdw);
		}

	}


	public void setTransitionEvaluator(int itr, TransitionEvaluator eva) {
		evaluators[itr] = eva;
	}




	/*

	   void evalCharge() {
	      int nstate = astate.length;
	      int ntrans = atrans.length;

	      double[][] m = new double[ntrans + 1][nstate];
	      double[] rhs = new double[ntrans + 1];
	      for (int i = 0; i < ntrans; i++) {
	         m[i][atrans[i].stateA.blockIndex] = 1.;
	         m[i][atrans[i].stateB.blockIndex] = -1.;
	         rhs[i] = atrans[i].getZ();
	      }

	      // add an equation to break the degeneracy saying the average is zero;
	      for (int j = 0; j < nstate; j++)
	         m[ntrans][j] = 1.;

	      double[][] mt = SMatrix.transpose(m);
	      double[][] mtm = SMatrix.MMmultiply(mt, m);
	      double[] mtr = SMatrix.MVmultiply(mt, rhs);

	      gatingCharge = SMatrix.LUSolve(mtm, mtr);
	   }



	   double getGatingCharge(int istate) {
	      return gatingCharge[istate];
	   }


	   double getGatingCharge(double[] d) {
	      double ret = 0.;
	      for (int i = 0; i < d.length; i++)
	         ret += d[i] * gatingCharge[i];
	      return ret;
	   }


	   double getRelativeConductance(int istat) {
	      double g = astate[istat].conductance;
	      g = Math.pow(g, power);
	      return g;
	   }


	   double[] getStateConductances() {
	      int ns = astate.length;
	      double[] ret = new double[ns];
	      for (int i = 0; i < ns; i++) {
	         ret[i] = astate[i].conductance;
	      }
	      return ret;
	   }


	   double getRelativeConductance(double[] fp) {
	      double ret = 0.;
	      for (int i = 0; i < fp.length; i++)
	         ret += fp[i] * astate[i].conductance;
	      ret = Math.pow(ret, power);
	      return ret;
	   }





	   public String getSummary() {
	       StringBuffer sb = new StringBuffer();
	       sb.append(name + " nstate=" + astate.length + "\n");
	       int icc = 0;
	       for (ChannelConformation cc : astate) {
	          sb.append("state " + icc + " " + cc.name + " g=" + cc.conductance + ";  ");
	          cc.setWork(icc);
	          icc++;
	          sb.append("\n");
	       }

	       for (ChannelTransition ct : atrans) {
	          sb.append(" " + ct.stateA.work + " --> " + ct.stateB.work);
	          sb.append(" " + ct.getParamSummary() + "\n");
	       }
	       return sb.toString();
	   }


	*/
}
