package org.psics.model.neuroml;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.ImportException;
import org.psics.be.Meta;
import org.psics.be.MetaContainer;
import org.psics.be.MetaItem;
import org.psics.model.channel.ClosedState;
import org.psics.model.channel.GatingComplex;
import org.psics.model.channel.KSChannel;
import org.psics.model.channel.KSComplex;
import org.psics.model.channel.KSTransition;
import org.psics.model.channel.OpenState;
import org.psics.model.neuroml.lc.current_voltage_relation;
import org.psics.model.neuroml.lc.ks_gate;


public class ChannelMLChannelType implements AddableTo, MetaContainer {

	public String name;
	public boolean density;

	public ChannelMLStatus status;

	public ChannelMLCVRelation current_voltage_relation;


	public ArrayList<ChannelMLKSGate> ksgates = new ArrayList<ChannelMLKSGate>();

	public ArrayList<ChannelMLHHGate> hhgates = new ArrayList<ChannelMLHHGate>();

	public ChannelMLImplPrefs impl_prefs;

	Meta meta;




	public void addMetaItem(MetaItem mi) {
		if (meta == null) {
			meta = new Meta();
		}
		meta.add(mi);
	}



	public void add(Object obj) {
		if (obj instanceof ChannelMLKSGate) {
			ksgates.add((ChannelMLKSGate)obj);

		} else if (obj instanceof ChannelMLHHGate) {
				hhgates.add((ChannelMLHHGate)obj);


		} else {
			E.typeError(obj);
		}
	}



	public KSChannel makeKSChannel() throws ImportException {
		KSChannel ret = new KSChannel();
		ret.setID(name);
		ret.setIon(ChannelMLIon.getSymbol(current_voltage_relation.getIonName()));

		HashMap<String, KSComplex> compHM = new HashMap<String, KSComplex>();


		if (ksgates.size() > 0) {
			HashMap<String, Double> relConds = new HashMap<String, Double>();

			for (ChannelMLGate cmlg : current_voltage_relation.getGates()) {
				cmlg.addOpenStates(relConds);
			}


			for (ChannelMLKSGate ckg : ksgates) {
				ret.add(ckg.makeKSComplex(relConds));
			}


		} else {

		for (ChannelMLGate cmlg : current_voltage_relation.getGates()) {
			KSComplex ksc = new KSComplex();
			String snm = cmlg.getStateName();
			ksc.setID(snm);
			ksc.addState(new ClosedState(snm + "c"));
			ksc.addState(new OpenState(snm + "o"));
			ksc.setInstances(cmlg.getPower());
			compHM.put(snm, ksc);
			ret.add(ksc);
		}


		for (ChannelMLHHGate g : hhgates) {

			if (g.isVoltage()) {
				String s = g.getState();
				KSTransition kst = g.getVoltageGate().getForwardTransition();
				if (kst != null) {
					kst.setFrom(s + "c");
					kst.setTo(s + "o");
					compHM.get(s).add(kst);
				}

				KSTransition kstr = g.getVoltageGate().getReverseTransition();
				if (kstr != null) {
					// the getForward method might have returned a two-way transition, in which case
					// there is no reverse
					kstr.setFrom(s + "o");
					kstr.setTo(s + "c");
					compHM.get(s).add(kstr);
				}
			}
		}
		}



		// these may have Q10s for
		for (ChannelMLRateAdjustments ra : current_voltage_relation.getRateAdjustments()) {
			 ra.applyTo(ret);
		}

		return ret;
	}



	public void populateFrom(KSChannel ksc) {
		 name = ksc.getID();

		 ksc.resolve();

		 String osid = ksc.getOpenStateID();

		 current_voltage_relation = new current_voltage_relation();
		 current_voltage_relation.setOhmicEtc(osid, ksc.getPermeantIonID());


		for (GatingComplex gc : ksc.getGatingComplexes()) {
			ks_gate g = new ks_gate();
			ksgates.add(g);
			g.populateFrom(gc);
		}
	}


}
