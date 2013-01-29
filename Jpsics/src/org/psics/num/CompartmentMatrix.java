package org.psics.num;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.psics.be.E;
import org.psics.num.model.channel.ChannelData;
import org.psics.num.model.channel.TableChannel;
import org.psics.num.model.synapse.SynapseData;
import org.psics.num.model.synapse.TableSynapse;
import org.psics.util.TextDataWriter;

public class CompartmentMatrix {

	final static int IOK = 0;
	final static int IERROR = 1;

	static double fcn = 0.51;
	// default weighting factor for temporal differencing. O.5 is Crank Nicolson, 1.0 implict Euler.
	// slightly above 0.5 is generally preferable as it cuts out the CN oscillations
	// (although in a timestep dependent way)

	int nc;
	Compartment[] cpts;

	int inpulse = 0;

	public void setWeightingFactor(double f) {
		fcn = f;
		// E.info("Changed fcn " + f);
	}


	public void importCompartments(Compartment[] ac, Compartment root) {
		clearFlags(ac);

		// may not want ordering in ac - impose our own depth first ordering
		ArrayList<Compartment> seqL = new ArrayList<Compartment>();

		/*
		// this is one way to get bath capacitances in and a set of connections to hold channel currents
		Compartment bath = new Compartment();
		seqL.add(bath);

		for (Compartment c : ac) {
			CompartmentConnection bcon = new CompartmentConnection(bath, c, 0.);
			bath.addConnection(bcon);
			c.addConnection(bcon);
		}
		*/



		stackSequence(root, seqL);


		// calculation uses local index arrays, but stores the work variables
		nc = seqL.size();
		cpts = seqL.toArray(new Compartment[nc]);
		for (int i = 0; i < nc; i++) {
			cpts[i].setIndex(i);
		}


		for (int i = 0; i < nc; i++) {
			// split the connections into two arrays - those lower in the sequence, and those higher;
			cpts[i].orderConnections();
		}
	}




	   /*
	    * The right hand side and diagonal elements are stored in the compartments
	    * cpts[i].rhs, cpts[i].diag, and the off diagonal elements are stored in the
	    * link: link.wsA, link.wsB. The elements to the left of the diagonal in row
	    * i are stored in node[i].downLinks[*].wsB, those to the right in
	    * node[i].upLinks[*].wsA;
	    *
	    * As the matrix is filled (from the bottom) elements to the right of the
	    * diagonal are eliminated. They only involve rows further down which have
	    * already been done. This leaves a lower trinagular matrix which then is
	    * solved by backsubstitution from the top down.
	    *
	    * The code is slightly more general than conventional applications - it will
	    * allow any number of full rows and columns, as long as they appear before
	    * all the rest. Eg, in a single cell context, it is not necessary that the
	    * bath be earthed.
	    *
	    */
	public int diffuse(double dt) {
	      int iret = IOK;

	      cpts[0].rhs = 0.;

	      if (cpts[0].appliedCurrent > 0) inpulse ++;



	      for (int ic = nc - 1; ic >= 0; ic--) {
	         Compartment cpt = cpts[ic];


	         if (cpt.clamped) {
	        	 cpt.v = cpt.clampValue;
	        	 cpt.diag = 1.;
	        	 cpt.rhs = 0.;

	         } else {
	        	 cpt.diag = cpt.capacitance;
	        	 cpt.rhs = cpt.appliedCurrent * dt;

	        	 double cgdt = dt * cpt.gChan;
	             cpt.rhs += cgdt * (cpt.eChan - cpt.v);
	             cpt.diag += fcn * cgdt;

	        	 int ndown = cpt.preCon.length;
	        	 int nup = cpt.postCon.length;

	            // conductances to nodes earlier in list
	            for (int jd = 0; jd < ndown; jd++) {
	               CompartmentConnection con = cpt.preCon[jd];
	               double gdt = dt * con.conductance;
	               cpt.rhs += gdt * (con.from.v - cpt.v); //  - con.drive);
	               double cpgt = con.capacitance + gdt;
	               cpt.diag += fcn * cpgt;
	               con.workTo = -fcn * cpgt;
	            }

	            // and to nodes later in list
	            for (int ju = 0; ju < nup; ju++) {
	               CompartmentConnection con = cpt.postCon[ju];
	               double gdt = dt * con.conductance;
	               cpt.rhs += gdt * (con.to.v - cpt.v); //  + con.drive);
	               double cpgt = con.capacitance + gdt;
	               cpt.diag += fcn * cpgt;
	            }

	            if (cpt.diag == 0.) {
	            	E.error("no contribution to diag compartment? " + ic);
	            }


	            if (cpt.appliedConductance > 0.) {
	               double gdt = dt * cpt.appliedConductance;
	               cpt.rhs += gdt * (cpt.appliedDrive - cpt.v);
	               cpt.diag += fcn * gdt;
	            }


	            // eliminate all points to the right of the leading diagonal
	            for (int ju = nup - 1; ju >= 0; ju--) {
	               CompartmentConnection con = cpt.postCon[ju];
	               Compartment tocpt = con.to;

	               // using reduced row k to eliminate col k in row i
	               if (tocpt.diag == 0.) {
	                  E.error("error - zero diag elt " + tocpt);
	                  iret = IERROR;

	               }

	               double ff = con.workTo / tocpt.diag;

	               // the rhs
	               cpt.rhs -= ff * tocpt.rhs;

	               // eliminate to left of diagonal at k; - ignore bath
	               int nkl = tocpt.preCon.length;
	               for (int m = 0; m < nkl; m++) {
	                  cpt.diag -= ff * tocpt.preCon[m].workTo;
	               }
	            }
	         }
	      }

	      // backsubstitute
	      for (int i = 0; i < nc; i++) {
	    	  Compartment cpt = cpts[i];

	         int npre = cpt.preCon.length;
	         for (int j = 0; j < npre; j++) {
	            cpt.rhs -= cpt.preCon[j].workTo * cpt.preCon[j].from.rhs;
	         }
	         cpt.rhs /= cpt.diag;
	         cpt.v += cpt.rhs;
	      }

	     //  calculateCurrents(cpt);
	      return iret;
	   }





	// NB this is the preferred way to walk the tree depth first - recursion would make for shorter
	// code, but you can overdo the java stack if there are very long processes (projecting axons are
	// most likely to show the problem).
	private void stackSequence(Compartment root, ArrayList<Compartment> seqL) {
		Stack<Compartment> stack = new Stack<Compartment>();
		stack.push(root);

		while (!stack.empty()) {
			Compartment cpt = stack.pop();
			seqL.add(cpt);
			cpt.wkFlag = true;
			for (Compartment cn : cpt.getNeighbors()) {
				if (cn.wkFlag) {
					// already done;
				} else {
					stack.push(cn);
				}
			}
		}
	}



	private void clearFlags(Compartment[] ac) {
		for (int i = 0; i < ac.length; i++) {
			ac[i].wkFlag = false;
		}
	}




	public void allocateChannels(ChannelData channelData) {
		HashMap<String, TableChannel> channelHM = channelData.getChannelHM();

		for (Compartment cpt : cpts) {
			cpt.allocateChannels(channelHM);
		}
	}

	public void allocateSynapses(SynapseData synapseData) {
		HashMap<String, TableSynapse> synapseHM = synapseData.getSynapseHM();

		for (Compartment cpt : cpts) {
			cpt.allocateSynapses(synapseHM);
		}
	}

	public void instantiateChannels() {
		for (Compartment cpt : cpts) {
			cpt.instantiateChannels();
		}
	}


	public void advanceChannels(int stepType) {
		if (stepType != 0) {
			E.missing();
			// should go through the tables and set tye type (one of a few discrete timesteps) for this
			// step;
		}

		for (Compartment cpt : cpts) {
			cpt.advanceChannels();
		}

	}




	public void setPotential(double v) {
		 for (Compartment cpt : cpts) {
			 cpt.v = v;
		 }

	}
	  @SuppressWarnings("boxing")
	   public String getVText(double time) {
	      StringBuffer sb = new StringBuffer();
	      // TODO tag specific to integer quantities;
	      int nel = cpts.length;
	      int nspec = 1;

	      String[] vnames = {"v"};

	      sb.append("gridConcentrations " + nel + " " + nspec + " " + time + " ");
	      for (int i = 0; i < nspec; i++) {
	         sb.append(vnames[i] + " ");
	      }
	      sb.append("\n");

	      for (int i = 0; i < nel; i++) {
	         for (int j = 0; j < nspec; j++) {
	            sb.append(stringd(cpts[i].v));
	         }
	         sb.append("\n");
	      }
	      return sb.toString();
	   }

	  private String stringd(double d) {
	      if (d == 0.0) {
	         return "0.0 ";
	      } else {
	         return String.format("%.5g ", new Double(d));
	      }
	   }


	public void appendTo(TextDataWriter tdw, HashMap<String, Integer> channelNumIDs,
			HashMap<String, Integer> sypopNumIDs) {
		tdw.clearEco();
		tdw.addInts(nc);
		tdw.addMeta("n compartments");

		for (Compartment c : cpts) {
			c.appendTo(tdw, channelNumIDs, sypopNumIDs);
		}
	}


	public void summarize(CalcSummary csum) {
		csum.setNCompartments(nc);
		int nsch = 0;
		int ncch = 0;
		int nscpt = 0;
		int nccpt = 0;

		int nngch = 0;
		int nngcpt = 0;

		for (Compartment cpt : cpts) {
			int n = cpt.getNStochasticChannels();
			if (n > 0) {
				nscpt += 1;
				nsch += n;
			}

			n = cpt.getNNonGatedChannels();
			if (n > 0) {
				nngcpt += 1;
				nngch += n;
			}

			n = cpt.getNContinuousChannels();
			if (n > 0) {
				nccpt += 1;
				ncch += n;
			}
		}
		csum.setNChannelsStoch(nsch);
		csum.setNChannelsCont(ncch);
		csum.setNChannelsNg(nngch);

		csum.setNCompartmentsStoch(nscpt);
		csum.setNCompartmentsCont(nccpt);
		csum.setNCompartmentsNg(nngcpt);
	}


}
