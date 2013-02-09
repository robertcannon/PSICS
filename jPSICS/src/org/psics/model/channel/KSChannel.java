package org.psics.model.channel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.psics.be.AddableTo;
import org.psics.be.ContainerForm;
import org.psics.be.E;
import org.psics.be.LongNamed;
import org.psics.be.Standalone;
import org.psics.be.TextForm;
import org.psics.model.control.About;
import org.psics.model.environment.Ion;
import org.psics.model.math.Function;
import org.psics.num.model.channel.TableChannel;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.phys.Conductance;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.units.Units;


@ModelType(standalone=true, usedWithin={},
		tag="An ion channel represented by a kinetic scheme", info="Also known as a Markov model," +
		"the schem defines the possible states (configurations) of the channel and the transitions " +
		"between them. If the scheme falls into disjoint sub-schemes these are interpreted as " +
		"independent serial gating complexes and the effective relative conductance os the product " +
		"of the relative conductances of separate complexes. This enables Hodgkin Huxley style" +
		"to be economically represented as a collection of separate two-state complexes")
public class KSChannel extends BaseChannel implements AddableTo, Standalone, LongNamed {
	
	@TextForm(pos=2, label="single channel conductance", ignore="")
	@Quantity(range = "(0.1, 100)", required = true, tag = "Single channel conductance. Separate open states can" +
			"specify their relative conductance as a fraction of this quantity.", units = Units.pS)
	public Conductance gSingle;

	@TextForm(pos=1, label="is permeable to $ ions", ignore="")
	@ReferenceByIdentifier(location=Location.indirect, required = true, tag = "The permeant ion", targetTypes = { Ion.class })
	public String permeantIon;
	public Ion r_permeantIon;
		
	@Container(tag="list of states", contentTypes={OpenState.class, ClosedState.class})
	public ArrayList<KSState> c_states = new ArrayList<KSState>();

	@Container(tag="list of transitions", contentTypes={FixedRateTransition.class,
			VHalfTransition.class, VRateTransition.class, ExpLinearTransition.class})
	public ArrayList<KSTransition> c_transitions = new ArrayList<KSTransition>();

	@ContainerForm(pos=10, label="gating complex[|es]", unwrapone=true)
	@Container(tag="gating complexes", contentTypes={KSComplex.class})
	public ArrayList<KSComplex> c_complexes = new ArrayList<KSComplex>();

   

	@Container(tag="Conductance model - just one per channel",
			contentTypes={OhmicConductanceModel.class })
	public ArrayList<ConductanceModel> c_conductanceModels = new ArrayList<ConductanceModel>();

	@Container(tag="(deprecated) Additional coded functions, if any, needed for coded transitions.",
			contentTypes={CodedTransitionFunction.class })
	public ArrayList<CodedTransitionFunction> c_functions = new ArrayList<CodedTransitionFunction>();


	@Container(tag="Additional functions, if any, needed for transitions.",
			contentTypes={Function.class })
	public ArrayList<Function> c_funcs = new ArrayList<Function>();


	private ArrayList<GatingComplex> p_gcomplexes = new ArrayList<GatingComplex>();

	private boolean p_resolved = false;

	private boolean p_valid = true;

	

	public String getID() {
		return id;
	}


	public void add(Object obj) {
		if (obj instanceof KSState) {
			c_states.add((KSState)obj);
		} else if (obj instanceof KSTransition) {
			c_transitions.add((KSTransition)obj);

		} else if (obj instanceof KSComplex) {
			c_complexes.add((KSComplex)obj);

		} else if (obj instanceof CodedTransitionFunction) {
			c_functions.add((CodedTransitionFunction)obj);

		} else if (obj instanceof Function) {
			c_funcs.add((Function)obj);

		} else if (obj instanceof About) {
			c_abouts.add((About)obj);


		} else {
			E.warning("cant add " + obj);
		}
	}


	public String getLongName() {
		return "kinetic scheme ion channel";
	}

	// TODO could generalize, reflect...?
	public KSChannel deepCopy() {
		KSChannel ret = new KSChannel();
		ret.gSingle = gSingle.makeCopy();
		ret.permeantIon = permeantIon;
		ret.r_permeantIon = r_permeantIon;

		HashMap<KSState, KSState> stateHM = new HashMap<KSState, KSState>();
		for (KSState s : c_states) {
		    KSState sn = s.deepCopy();
			ret.add(sn);
			stateHM.put(s, sn);
		}
		for (KSTransition t : c_transitions) {
			ret.add(t.deepCopy(stateHM.get(t.getFrom()), stateHM.get(t.getTo())));
		}

		for (KSComplex c : c_complexes) {
			ret.add(c.deepCopy());
		}
		for (CodedTransitionFunction ctf : c_functions) {
			ret.add(ctf.deepCopy());
		}
		return ret;
	}









	public boolean isValid() {
		 return p_valid;
	}

	// Using lists here rather than sets so that the order in the final tables is determined
	// by the order of the transitions in the model specification

	public boolean nonTrivial() {
		return (c_transitions.size() > 0 || c_complexes.size() > 0);
	}



	private ArrayList<KSTransition> getAllTransitions() {
		ArrayList<KSTransition> ret = new ArrayList<KSTransition>();
		ret.addAll(c_transitions);
		for (KSComplex cplx : c_complexes) {
			ret.addAll(cplx.getTransitions());
		}
		return ret;
	}


	public void resolve() {
		p_valid = true;

		int nctr = 0;
		// set sysetm ids for coded transitions - these get used as the class name when they are compiled.
		// if ther are any channel-wide functions, give them to all the coded transitions (they may
		// not all need them, but we don't kow and it won't hurt).

		for (KSTransition trans : getAllTransitions()) {
			if (trans instanceof CodedTransition) {
				CodedTransition ctr = (CodedTransition)trans;
				if (c_functions != null) {
					ctr.setFunctions(c_functions);
				}

				ctr.setSysID(getID() + "_" + nctr);
				nctr += 1;
			}

			if (trans instanceof FunctionTransition) {
				((FunctionTransition)trans).addFunctions(c_funcs);
			}

		}



		if (c_transitions.size() == 0 && c_complexes.size() == 0) {
			return;
		}

		for (KSTransition trans : c_transitions) {
			boolean ok = trans.linkStates();
			if (!ok) {
				p_valid = false;
			}
		}
		HashSet<KSState> statewk = new HashSet<KSState>();
		for (KSTransition kst : c_transitions) {
			kst.setUndone();
			if (statewk.contains(kst.getFrom())) {
				// Ok
			} else {
				statewk.add(kst.getFrom());
			}
			if (statewk.contains(kst.getTo())) {
				// Ok
			} else {
				statewk.add(kst.getTo());
			}
		}



		if (statewk.size() != c_states.size()) {
			E.error("State miscount: transitions reference " + statewk.size() + " states " +
					" but declaration has " + c_states.size());
		}

		p_gcomplexes = new ArrayList<GatingComplex>();

		while (statewk.size() > 0) {
//			E.info("starting a new gating complex " + statewk.size());
			GatingComplex gc = new GatingComplex();
			p_gcomplexes.add(gc);

			// find the first unprocessed transition and put its ends in;
			KSTransition startTrans = null;
			for (KSTransition t : c_transitions) {
				if (!t.done()) {
					startTrans = t;
					gc.add(t);
					gc.add(t.getFrom());
					gc.add(t.getTo());
					t.setDone();
					// E.info("found start transition " + t + " " + statewk.size());
					statewk.remove(t.getFrom());
					statewk.remove(t.getTo());
					// E.info("beginning extension " + statewk.size());
					break;
				}
			}

			if (startTrans == null) {
				E.error("no free transition for next gating complex, but the states have not been used up??");
			}


			boolean added = true;
			while (added) {
				added = false;
				for (KSTransition t : c_transitions) {
					if (!t.done()) {
						if (gc.contains(t.getFrom())) {
							gc.add(t);
							gc.add(t.getTo());
							t.setDone();
							added = true;
							statewk.remove(t.getTo());

						} else if (gc.contains(t.getTo())) {
							gc.add(t);
							gc.add(t.getFrom());
							t.setDone();
							added = true;
							statewk.remove(t.getFrom());
					 	}
					}
				}
			}
			if (p_gcomplexes.size() > 10) {
				E.warning("more than ten gating compexes? - probably a referencing error...");
				break;
			}
		}


		for (KSComplex cpx : c_complexes) {
			GatingComplex gc = cpx.getGatingComplex();

			p_gcomplexes.add(gc);

			/* 05 JULY - not duplicating nulti-instance complexes at this level
			for (int ic = 0; ic < cpx.getNInstances(); ic++) {
				gcomplexes.add(gc); // POSERR - save to add the same complex specification? or need copies?
			}
			*/
		}
//		E.log("Channel info: " + printResolved());
		p_resolved = true;
	}



	public String printResolved() {
		StringBuffer sb = new StringBuffer();
		int ngc = p_gcomplexes.size();
		sb.append("  KSChannel id=" + id + " gSingle=" + gSingle.getValue(Units.pS) + "pS" +
				",  " + (ngc > 1 ? "" + ngc + " gating complexes." :  "one gating complex: ") + " ");
		if (ngc == 1) {
			sb.append(p_gcomplexes.get(0).printStructure());

		} else {
			int igc = 0;
			for (GatingComplex gc : p_gcomplexes) {
				sb.append("complex " + igc + ": ");
				igc += 1;
				sb.append(gc.printStructure());
				sb.append("\n");
			}
		}
		return sb.toString();
	}


	public boolean isMultiComplex() {
		boolean ret = false;
		if (p_gcomplexes.size() > 1) {
			ret = true;
		} else if (p_gcomplexes.size() == 1 && p_gcomplexes.get(0).nInstances > 1) {
			ret = true;
		}
		return ret;
	}


	public TableChannel tablifyAsSingleComplex() {
		return tablify(true);
	}

	public TableChannel tablifyMultiComplex() {
		TableChannel ret = null;
		if (isMultiComplex()) {
			ret = tablify(false);
			ret.setID(id + "-mc");
		} else {
			E.warning("no multi-complex form for " + this);
		}
		return ret;
	}

	public void checkResolved() {
	if (!p_resolved) {
		resolve();
	}
	}


	private TableChannel tablify(boolean bsingle) {
		checkResolved();
		TableChannel ret = new TableChannel(id);

		if (nonTrivial()) {

			boolean bdone = false;

			if (bsingle && (p_gcomplexes.size() > 1 || p_gcomplexes.get(0).nInstances > 1)) {
				GatingComplex gc = getEquivalentSingleGatingComplex();
				if (gc != null) {
					ret.allocateGatingComplexes(1);
					ret.setGatingComplex(0, gc.tablify());
					bdone = true;
				}
			}


			if (!bdone) {
				ret.allocateGatingComplexes(p_gcomplexes.size());
				for (int i = 0; i < p_gcomplexes.size(); i++) {
					ret.setGatingComplex(i, p_gcomplexes.get(i).tablify());
				}
			}
		} else {
			ret.setNonGated();
		}
		ret.setBaseConductance(gSingle);
		return ret;
	}


	public Ion getPermeantIon() {
		E.missing();
		//wont  work - not set by default - should it be, or do we just get the id?

		return r_permeantIon;
	}


	public String getPermeantIonID() {
		return permeantIon;
	}



	public GatingComplex getEquivalentSingleGatingComplex() {

		int ns = 1;
		int nmi = 0;
		KSComplex kscmi = null;
		for (KSComplex ksc : c_complexes) {
			if (ksc.c_states.size() > 2) {
				E.warning("can't get single equlvalent complex - too many substates");
				return null;
			} else {
				int nin = ksc.instances.getNativeValue();
				if (nin > 1) {
					ns = ns * (1 + nin);
					kscmi = ksc;
					nmi += 1;
				} else {
					ns = ns * 2;
				}
			}
		}
		if (nmi > 1) {
			E.warning("Channel has too many mutlti-instance complexes - cant make single equivalent");
			return null;

		} else if (kscmi != null) {
			// has to go first for subsequent processing
			c_complexes.remove(kscmi);
			c_complexes.add(0, kscmi);
		}

		// E.info("equivalent single complex has " + ns + " states");

		ArrayList<KSTransition> newtrans = new ArrayList<KSTransition>();
		OpenState[]wkstates = new OpenState[ns];
		wkstates[0] = new OpenState(1.0);
		int nwks = 1;

		for (KSComplex ksc : c_complexes) {
			ClosedState cs;
			OpenState os;
			if (ksc.c_states.get(0) instanceof ClosedState) {
				cs = (ClosedState)(ksc.c_states.get(0));
				os = (OpenState)(ksc.c_states.get(1));

			} else {
				cs = (ClosedState)ksc.c_states.get(1);
				os = (OpenState)ksc.c_states.get(0);
			}
			KSTransition fwd = ksc.getFromToTransition(cs, os);
			KSTransition rev = ksc.getFromToTransition(os, cs);


			int n = ksc.instances.getNativeValue();

			if (n <= 1) {
				int noldtrans = newtrans.size();
				for (int i = 0; i < nwks; i++) {
					wkstates[nwks + i] = new OpenState("s" + (nwks + i), wkstates[i].getRelativeConductance() * os.getRelativeConductance());
					wkstates[i].setRelativeConductance(0.);
					if (fwd != null) {
						newtrans.add(fwd.makeCopy(wkstates[i], wkstates[nwks+i]));
					}
					if (rev != null) {
						newtrans.add(rev.makeCopy(wkstates[nwks+i], wkstates[i]));
					}
				}
				// now replicate the transition structure within the original states in the new ones
				for (int i = 0; i < nwks; i++) {
					wkstates[i].setWork(i);
				}


				for (int itrans = 0; itrans < noldtrans; itrans++) {
					KSTransition tro = newtrans.get(itrans);
					KSState sa = tro.getFrom();
					KSState sb = tro.getTo();
					int isa = sa.getWork();
					int isb = sb.getWork();

					KSTransition tf = getFromToTransition(newtrans, sa, sb);
					// KSTransition tr = getFromToTransition(newtrans, sb, sa);
					if (tf != null) {
						newtrans.add(tf.makeCopy(wkstates[nwks+isa], wkstates[nwks+isb]));
					}
					/* MAr 05 2008  - this shouldn't be there: the transition array already
					 * contains forward and reverse transitions duplicated from the originals
					if (tr != null) {
						newtrans.add(tr.makeCopy(wkstates[nwks+isb], wkstates[nwks+isa]));
					}
					*/

				}
				nwks = nwks + nwks;


			} else {
				// only occurs once, as the first complex, if at all
				for (int i = 0; i < n; i++) {
					wkstates[i] = new OpenState("s" + i, 0.);
				}
				wkstates[n] = new OpenState("s" + n, os.getRelativeConductance());
				for (int i = 0; i < n; i++) {
					double ff = (n - i);
					double fr = (i + 1);

					if (fwd != null) {
						newtrans.add(fwd.makeMultiCopy(wkstates[i], wkstates[i+1], ff, fr));
					}
					if (rev != null) {
						newtrans.add(rev.makeMultiCopy(wkstates[i+1], wkstates[i], fr, ff));
					}
				}
				if (nwks != 1) {
					E.fatalError("multi state HH conversion");
				}
				nwks = (n + 1);
			}
		}

		if (nwks != wkstates.length) {
			E.error("state miscount " + nwks + " " + wkstates.length);
		}

		GatingComplex ret = new GatingComplex();
		for (int i = 0; i < wkstates.length; i++) {
			ret.add(wkstates[i]);
		}
		for (KSTransition trans : newtrans) {
			ret.add(trans);
		}


		return ret;
	}



	private KSTransition getFromToTransition(ArrayList<KSTransition> atrans, KSState sa, KSState sb) {
		KSTransition ret = null;
		for (KSTransition tr : atrans) {
			if (tr.isFromTo(sa, sb)) {
				ret = tr;
			}
		}
		return ret;
	}



	public ArrayList<String> getInfoParas() {

		ArrayList<String> ret = new ArrayList<String>();
		if (c_abouts != null) {
			for (About ab : c_abouts) {
				ret.add(ab.getText());
			}
		}
		return ret;
	}


	public void setID(String s) {
		id = s;
	}

	public void setIon(String s) {
		permeantIon = s;
	}


	public void setComplexQ10(String sg, double f, Temperature t) {
		boolean done = false;
		for (KSComplex c : c_complexes) {
			if (sg.equals(c.getID())) {
				for (KSTransition tr : c.getTransitions()) {
					tr.setQ10(f, t);
					done = true;
				}
			}
		}
		if (!done) {
			E.error("no such complex " + sg + " when setting q10");
		}
	}


	public void setGlobalQ10(double f, Temperature t) {
		for (KSTransition kst : getAllTransitions()) {
			kst.setQ10(f, t);
		}
	}


	public String getOpenStateID() {
		String ret = null;
		for (KSState s : c_states) {
			if (s instanceof OpenState) {
				ret = s.getID();
			}
		}
		if (ret == null) {
			for (KSComplex cplx : c_complexes) {
				ret = cplx.getOpenStateID();
				if (ret != null) {
					break;
				}
			}
		}
		return ret;
	}


	public ArrayList<GatingComplex> getGatingComplexes() {
		 return p_gcomplexes;
	}


}
