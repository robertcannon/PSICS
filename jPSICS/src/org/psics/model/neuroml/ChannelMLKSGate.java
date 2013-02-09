package org.psics.model.neuroml;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.ImportException;
import org.psics.model.channel.ClosedState;
import org.psics.model.channel.GatingComplex;
import org.psics.model.channel.KSComplex;
import org.psics.model.channel.KSTransition;
import org.psics.model.channel.OpenState;


import org.psics.model.channel.KSState;
import org.psics.model.neuroml.lc.state;
import org.psics.model.neuroml.lc.transition;

public class ChannelMLKSGate implements AddableTo {

	ArrayList<ChannelMLState> states = new ArrayList<ChannelMLState>();

	ArrayList<ChannelMLTransition> transitions = new ArrayList<ChannelMLTransition>();



	public void add(Object obj) {
		if (obj instanceof ChannelMLState) {
			states.add((ChannelMLState)obj);

		} else if (obj instanceof ChannelMLTransition) {
			transitions.add((ChannelMLTransition)obj);

		} else {
			E.typeError(obj);
		}
	}



	public KSComplex makeKSComplex(HashMap<String, Double> relConds) throws ImportException {
		KSComplex ksc = new KSComplex();
		for (ChannelMLState st : states) {
			String nm = st.getName();
			if (relConds.containsKey(nm)) {
				OpenState os = new OpenState(nm);
				os.setRelativeConductance(relConds.get(nm).doubleValue());
				ksc.add(os);

			} else {
				ClosedState cs = new ClosedState(nm);
				ksc.add(cs);
			}
		}

		for (ChannelMLTransition tr : transitions) {
			ChannelMLVoltageGate vg = tr.getVoltageGate();
			if (vg.isAlphaBeta()) {
				String state1 = tr.getSource();
				String state2 = tr.getTarget();
				KSTransition kstf = vg.getForwardTransition();
				if (kstf != null) {
					kstf.setFrom(state1);
					kstf.setTo(state2);
					ksc.add(kstf);
				}

				KSTransition kstr = vg.getReverseTransition();
				if (kstr != null) {
					kstr.setFrom(state2);
					kstr.setTo(state1);
					ksc.add(kstr);
				}

			} else {
				throw(new ImportException("unhandled transition type in ksgate"));
			}
		}
		return ksc;
	}


	public void populateFrom(GatingComplex gc) {
		 for (KSState s : gc.getStates()) {
			 state st = new state();
			 st.name = s.getID();
			 states.add(st);
		 }

		 for (KSTransition kst : gc.getTransitions()) {
			 transition tr = new transition();

			 tr.populateFrom(kst);
		 }

	}


}
