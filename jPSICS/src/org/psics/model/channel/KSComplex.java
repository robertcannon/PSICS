package org.psics.model.channel;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.be.ContainerForm;
import org.psics.be.E;
import org.psics.be.IDd;
import org.psics.be.LongNamed;
import org.psics.be.TextForm;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.units.Units;


@ModelType(standalone=true, usedWithin={KSChannel.class},
		tag="a single complex within a kinetic scheme channel", info="KSComplexes provide an optional grouping of " +
				"states within a channel. The effect is the same as specifying transitions between top-level " +
				"that implicitly define separate gating complexes, with the exception that the complex " +
				"also allows an <x>instances</x> attribute that can be used to specify that the channel has  more tna one " +
				"sequential instance of the same complex. This is primarily useful for the economical representation of " +
				"Hodgkin-Huxley style channels that have multiple independent, but identically gated, two-state complexes.")
public class KSComplex implements AddableTo, IDd, LongNamed {

	@TextForm(pos=-1, label="", ignore="")
	@Identifier(tag="Identifier (name) for the complex")
	public String id;

	@ContainerForm(label = "state[|s]", pos = 1, unwrapone = false)
	@Container(tag="list of states", contentTypes={OpenState.class, ClosedState.class})
	public ArrayList<KSState> c_states = new ArrayList<KSState>();
	
	@ContainerForm(label = "transition[|s]", pos = 2, unwrapone = false)
	@Container(tag="list of transitions", contentTypes={FixedRateTransition.class,
			VHalfTransition.class, VRateTransition.class, ExpLinearTransition.class})
	public ArrayList<KSTransition> c_transitions = new ArrayList<KSTransition>();

	@TextForm(label="occurs $ times", pos=0, ignore="1")
	@IntegerNumber(range="[1,4)", required=false, tag="number of complexes of this type in the channel, equivalent" +
			"to the power used in HH models")
	public NDNumber instances = new NDNumber(1);


	private boolean p_resolved = false;


	GatingComplex gComplex;


	public String getLongName() {
		return "complex";
	}


	public String getID() {
		return id;
	}


	public void add(Object obj) {
		if (obj instanceof KSState) {
			c_states.add((KSState)obj);

		} else if (obj instanceof KSTransition) {
			c_transitions.add((KSTransition)obj);

		} else {
			E.warning("cant add " + obj);
		}
	}




	public void resolve() {
		// valid = true;
		if (c_transitions.size() == 0) {
			return;
		}

		for (KSTransition trans : c_transitions) {
			boolean ok = trans.linkStates();
			if (!ok) {
			//	valid = false;
			}
		}



		gComplex = new GatingComplex();

		for (KSState s : c_states) {
			gComplex.add(s);
		}

		for (KSTransition t : c_transitions) {
			gComplex.add(t);
		}

		gComplex.setNInstances(getNInstances());

		// TODO more erro checking
		// valid = true;
		p_resolved = true;
	}

	int getNInstances() {
		int ret = 1;
		int nin = instances.getNativeValue();
		if (nin > 1) {
			ret = nin;
		}
		return ret;
	}

	public String printResolved() {
		StringBuffer sb = new StringBuffer();
		sb.append(gComplex.printStructure());
		return sb.toString();
	}



	public GatingComplex getGatingComplex() {
		if (!p_resolved) {
			resolve();
		}
		return gComplex;
	}


	public KSTransition getFromToTransition(KSState sa, KSState sb) {
		KSTransition ret = null;
		for (KSTransition tr : c_transitions) {
			if (tr.isFromTo(sa, sb)) {
				ret = tr;
			}
		}
		return ret;
	}


	public ArrayList<KSTransition> getTransitions() {
		 return c_transitions;
	}


	public KSComplex deepCopy() {
		KSComplex ret = new KSComplex();
		if (instances != null) {
			ret.instances = instances.makeCopy();
		}
		ret.id = id;

		HashMap<KSState, KSState> stateHM = new HashMap<KSState, KSState>();
		for (KSState s : c_states) {
		    KSState sn = s.deepCopy();
			ret.add(sn);
			stateHM.put(s, sn);
		}
		for (KSTransition t : c_transitions) {
			ret.add(t.deepCopy(stateHM.get(t.getFrom()), stateHM.get(t.getTo())));
		}
		return ret;
	}


	public void setInstances(int p) {
		instances.setIntValue(p, Units.none);
	}


	public void addState(KSState state) {
		 add(state);
	}


	public void setID(String sid) {
		id = sid;
	}


	public String getOpenStateID() {

			String ret = null;
			for (KSState s : c_states) {
				if (s instanceof OpenState) {
					ret = s.getID();
				}
			}
		return ret;
	}


}
