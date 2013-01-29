package org.psics.model.channel;

import java.util.ArrayList;

import org.psics.be.ContainerForm;
import org.psics.be.TextForm;
import org.psics.num.model.channel.GCTable;
import org.psics.num.model.channel.TransitionType;


/* this is created by resolving a channel model. If adding complexes
 * explicitly to a model, use the KSComplex object.
 */

public class GatingComplex {

	@ContainerForm(label = "state[|s]", pos = 1, unwrapone = false)
	public ArrayList<KSState> states = new ArrayList<KSState>();
	
	@ContainerForm(label = "transition[|s]", pos = 2, unwrapone = false)
	public ArrayList<KSTransition> transitions = new ArrayList<KSTransition>();

	@TextForm(label="occurs $ times", pos=0, ignore="1")
	public int nInstances = 1;


	public GatingComplex() {
		states = new ArrayList<KSState>();
		transitions = new ArrayList<KSTransition>();
	}


	public void setNInstances(int ni) {
		 nInstances = ni;
	}


	public ArrayList<KSState> getStates() {
		return states;
	}

	public ArrayList<KSTransition> getTransitions() {
		return transitions;
	}

	public void addState(KSState state) {
		states.add(state);
	}

	public void addTransition(KSTransition trans) {
		transitions.add(trans);
	}


	public void add(KSTransition t) {
		transitions.add(t);
	}

	public void add(KSState s) {
		states.add(s);
	}

	public boolean contains(KSState s) {
		return (states.contains(s));
	}


	public GCTable tablify() {
		GCTable ret = new GCTable();
		ret.setNInstances(nInstances);
		ret.allocateStates(states.size());
		ret.allocateTransitions(transitions.size());

		for (int i = 0; i < states.size(); i++) {
			KSState st = states.get(i);
			st.setWork(i);

			ret.setStateData(i, st.getID(), st.getRelativeConductance());
		}

		for (int i = 0; i < transitions.size(); i++) {
			KSTransition t = transitions.get(i);
			TransitionType ct = t.getTransitionType();
			ret.setTransitionData(i, ct.getCode(), t.getFrom().getWork(), t.getTo().getWork(),
						t.getTransitionData());

			if (t instanceof CodedTransition) {
				ret.setTransitionEvaluator(i, ((CodedTransition)t).getEvaluator());
			} else if (t instanceof FunctionTransition) {
				ret.setTransitionEvaluator(i, (FunctionTransition)t);
			}
		}

		return ret;
	}


	public String printStructure() {
		StringBuffer sb = new StringBuffer();
		sb.append("States: ");
		for (KSState s : states) {
			sb.append(s.getID());
			if (s instanceof ClosedState) {
				sb.append("(0)");
			} else {
				sb.append("(" + ((OpenState)s).getRelativeConductance() + ")");
			}
			sb.append(" ");
		}
		sb.append("   transitions: ");
		for (KSTransition kst : transitions) {
			sb.append(kst.printStructure());
			sb.append(" ");
		}

		return sb.toString();
	}



}
