package org.psics.num.model.channel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.num.ChannelGE;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;
import org.psics.util.TextDataWriter;


public class ChannelData {

	Time timestep = new Time(0.1, Units.ms);
	Voltage vmin = new Voltage(-90., Units.mV);
	Voltage vmax = new Voltage(50., Units.mV);
	Voltage deltaV = new Voltage(1.5, Units.mV);
	Temperature temp = null;

	int dfltThreshold = 0;

	ArrayList<String> orderedIds;
	HashMap<String, TableChannel> channelHM = new HashMap<String, TableChannel>();
	boolean doneFix = false;


	public void addTableChannel(TableChannel tch) {
		if (doneFix) {
			E.error("cant add a channel after fixing order");
		}
		channelHM.put(tch.getID(), tch);
	}


	public void setTimestep(Time dt) {
		timestep = dt;
	}

	public void setDeltaV(Voltage dv) {
		deltaV = dv;
	}

	public void setVMin(Voltage v) {
		vmin = v;
	}

	public void setVMax(Voltage v) {
		vmax = v;
	}




	public void buildTables() {
		for (TableChannel tch : channelHM.values()) {
			if (tch.isGated()) {
				tch.buildTransitionTables(temp, timestep, vmin, vmax, deltaV);
			}
		}
	}

	public HashMap<String, Integer> getChannelNumIDs() {
		HashMap<String, Integer> ret = new HashMap<String, Integer>();
		for (String s : channelHM.keySet()) {
			ret.put(s, channelHM.get(s).getNID());
		}
		return ret;
	}
	
	
	public HashMap<String, TableChannel> getChannelHM() {
		return channelHM;
	}


	public void setThresholds(HashMap<String, Integer> threshHM) {
		for (String s : channelHM.keySet()) {
			if (threshHM.containsKey(s)) {
				channelHM.get(s).setStochThreshold(threshHM.get(s).intValue());
			} else {
				channelHM.get(s).setStochThreshold(dfltThreshold);
			}
		}
	}



	public void setDefaultThreshold(int st) {
		dfltThreshold = st;
	}



	public void fixOrder() {
		orderedIds = new ArrayList<String>();
	    orderedIds.addAll(channelHM.keySet());
	    Collections.sort(orderedIds);

	    // set numeric ids for all channels before serializing any since they can refer to each other
	    for (int i = 0; i < orderedIds.size(); i++) {
	    	TableChannel tch = channelHM.get(orderedIds.get(i));
	    	tch.setNID(i);
	    }
	}

	public void appendTo(TextDataWriter tdw) {
	    tdw.addInts(channelHM.size());
	    tdw.addMeta("n channel");



	    double rtemp = CalcUnits.getTemperatureValue(temp);
	    // now serialize them all
	    for (String sid : orderedIds) {
	    	TableChannel tch = channelHM.get(sid);
	    	tch.appendTo(tdw, rtemp);
 	    }

	}


	public void checkAltFormsMultiTemperature() {
		// this will overwrite the existing matrices so they need rebuilding after;
		for (String s : channelHM.keySet()) {
			String smc = s + "-mc";
			if (channelHM.containsKey(smc)) {
				E.info("checking alts " + s + " " + smc);

				TableChannel cs = channelHM.get(s);
				TableChannel cm = channelHM.get(smc);

				Temperature[] temps = {new Temperature(15., Units.K), new Temperature(30, Units.K)};
				Voltage[] vs = {new Voltage(-81.2, Units.mV), new Voltage(-23.45, Units.mV),
							new Voltage(13.56, Units.mV)};



				for (Temperature ttemp : temps) {
					cs.buildTransitionTables(ttemp, new Time(0.02, Units.ms),
							new Voltage(-80, Units.mV), new Voltage(60, Units.mV),
							new Voltage(0.1, Units.mV));

					for (Voltage v : vs) {
						ChannelGE ge1 = cs.getChannelTableGE(v);
						ChannelGE ge2 = cm.getChannelTableGE(v);

						if (Math.abs((ge1.g - ge2.g)/(ge1.g + ge2.g + 1.e-12)) > 1.e-6 ||
							Math.abs(ge1.e - ge2.e) > 1.e-3) {
							E.warning("GE difference for single/multi compex channels " + s + " " + smc);
							E.info("" + ge1.g + " " + ge1.e + " " + ge2.g + " " + ge2.e);

						}
					}
				}



			}
		}
	}

	public void checkAltForms() {
		for (String s : channelHM.keySet()) {
			String smc = s + "-mc";
			if (channelHM.containsKey(smc)) {
				TableChannel cs = channelHM.get(s);
				TableChannel cm = channelHM.get(smc);

				Voltage[] vs = {new Voltage(-79.2, Units.mV),
							new Voltage(-73.7, Units.mV),
							new Voltage(-60.3, Units.mV),
							new Voltage(-53.4, Units.mV),
							new Voltage(-23.45, Units.mV),
							new Voltage(13.56, Units.mV)};

					for (Voltage v : vs) {
						ChannelGE ge1 = cs.getChannelTableGE(v);
						ChannelGE ge2 = cm.getChannelTableGE(v);
						ChannelGE ge3 = cs.getChannelGE(v);
						ChannelGE ge4 = cm.getChannelGE(v);

						double fge = 2 * (ge1.g - ge2.g)/(ge1.g + ge2.g);
						if (Math.abs(fge) > 1.e-3 || Math.abs(ge1.e - ge2.e) > 1.e-3) {
							E.info("GE " + s + " " + smc + " difference for single/multi compex " +
									"channels: at v=" + v.getNativeValue() + "\n" +
									"f=" + fge + " " + ge1.g  + " " + ge2.g + " " + ge3.g + " " + ge4.g);

						}
					}

					/*
					ChannelSet csset = cs.makeChannelSet(1);
					ChannelSet cmset = cm.makeChannelSet(1);

					csset.instantiateChannels(-55);
					cmset.instantiateChannels(-55.);

					for (int i = 0; i < 3; i++) {
						csset.advance(-45. );
						cmset.advance(-45. );
						E.info(" " + i + " " + csset.getGEff() + " " + cmset.getGEff());
					}
					cs.printMatrix(-45);
					cm.printMatrix(-45);
					*/
			}

		}
	}



	public HashMap<String, ChannelGE> getGEHM(Voltage potential, Temperature temperature) {

		HashMap<String, ChannelGE> geHM = new HashMap<String, ChannelGE>();
		for (String s : channelHM.keySet()) {
			ChannelGE cge = channelHM.get(s).getChannelGE(potential, temperature);
			geHM.put(s, cge);
		}
		checkAltForms(geHM);
		return geHM;
	}




	private void checkAltForms(HashMap<String, ChannelGE> geHM) {
		for (String s : geHM.keySet()) {
			String smc = s + "-mc";
			if (geHM.containsKey(smc)) {
				ChannelGE ge1 = geHM.get(s);
				ChannelGE ge2 = geHM.get(smc);
				if (Math.abs((ge1.g - ge2.g)/(ge1.g + ge2.g + 1.e-6)) > 1.e-3) {
					E.error("equilibrium mismatch between single complex and multi-complex models:\n " +
							"single complex, g=" + ge1.g + "\nmulticomplex g=" + ge2.g);
				}
			}
		}
	}



	public void setTemperature(Temperature tp) {
		 temp = tp;

	}


	public int getChannelIndex(String ctyp) {
		 int ret = -1;
		 if (channelHM.containsKey(ctyp)) {
			 ret = channelHM.get(ctyp).getNID();
		 }
		 return ret;
	}

}
