package org.psics.model.neuroml;

import java.util.HashMap;


public class ChannelMLGate {

	public int power;

	public ChannelMLState state;

	public String getStateName() {
		 return state.getName();
	}

	public int getPower() {
		int ret = 1;
		if (power > 0) {
			ret = power;
		}
		return ret;
	}



	public void addOpenStates(HashMap<String, Double> relConds) {
	//	for (ChannelMLState st : states) {
		ChannelMLState st = state;

			if (st != null && st.getFraction() > 0) {
				relConds.put(st.getName(), new Double(st.getFraction()));
			}
	//	}

	}

}
