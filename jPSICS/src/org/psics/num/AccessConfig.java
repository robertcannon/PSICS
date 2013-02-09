package org.psics.num;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.num.model.channel.ChannelData;
import org.psics.util.TextDataWriter;


public class AccessConfig {

	ArrayList<Accessor> accessors = new ArrayList<Accessor>();

	ArrayList<Clamp> clamps = new ArrayList<Clamp>();

	double saveInterval = 0.1;

	boolean separateFiles = false;
	String sfExtension = ".txt";
	
	boolean recordClamps = false;

	public void addRecorder(String id, String at, LineStyle style) {
		 accessors.add(new VRecorder(id, at, style));
	}


	public void addVoltageClamp(String id, String at, CommandProfile[] profiles, LineStyle style, boolean brec) {
		VClamp vc = new VClamp(id, at, profiles, style, brec);
		accessors.add(vc);
		clamps.add(vc);

	}

	public void addCurrentClamp(String id, String at, CommandProfile[] profiles, LineStyle style, boolean brec) {
		CClamp cc = new CClamp(id, at, profiles, style, brec);
		accessors.add(cc);
		clamps.add(cc);
	}

	public void addConductanceClamp(String id, String at, double vto, CommandProfile[] profiles, LineStyle style, boolean brec) {
		GClamp gc = new GClamp(id, at, vto, profiles, style, brec);
		accessors.add(gc);
		clamps.add(gc);
	}



	public String[] getRecorderIDs() {
		String[] ret = new String[accessors.size()];
		int iv = 0;
		for (Accessor acc : accessors) {
			String sid = acc.getID();
			if (sid == null) {
				sid = acc.getAt();
			}
			ret[iv++] = sid;
		}
		return ret;

	}


	public void attachTo(CompartmentTree ctree, ChannelData channelData) {
		TreeMatcher tm = ctree.getTreeMatcher();

		ArrayList<Accessor> togo = new ArrayList<Accessor>();
		for (Accessor acc : accessors) {
			String atid = acc.getAt();
			Compartment cpt = tm.getIdentifiedCompartment(atid);
			if (cpt != null) {
				acc.setCompartment(cpt);
			} else {
				togo.add(acc);
				E.fatalError("an accessor (clamp or recorder) is defined for compartment " + atid +
						" but there no such compartment in structure\n All labels: " + tm.allLabels());
			}

			if (acc instanceof ChannelRecorder) {
				ChannelRecorder cr = (ChannelRecorder)acc;
				String ctyp = cr.getChannelType();
				int cind = channelData.getChannelIndex(ctyp);
				if (cind < 0) {
					E.fatalError("a channel recorder is defined for channel " + ctyp +
							" but there is no such channel");
				} else {
					cr.setChannelIndex(cind);
				}

			}

		}
		for (Accessor acc : togo) {
			accessors.remove(acc);
			if (acc instanceof Clamp) {
				clamps.remove(acc);
			}
		}

	}



	public void advanceControl(double xtime, double xdt) {
		for (Clamp c : clamps) {
			c.advanceControl(xtime, xdt);
		}
	}


	public double[] getRecorderValues() {
		double[] v = new double[accessors.size()];
		int iv = 0;
		for (Accessor acc : accessors) {
			v[iv++] = acc.getValue();
		}
		return v;
	}


	public void appendTo(TextDataWriter tdw) {
		tdw.addInts(clamps.size(), accessors.size() - clamps.size(), (separateFiles ? 1 : 0));
		tdw.addMeta("n clamp, n recorder, separate files flag");
		tdw.add(saveInterval);
		tdw.addMeta("save interval");
		if (separateFiles) {
			tdw.add(sfExtension);
			// tdw.addMeta("extension for separate files");
		}
		
		for (Clamp clamp : clamps) {
			clamp.appendTo(tdw);
		}
		for (Accessor acc : accessors) {
			if (acc instanceof Clamp) {
				// already done
			} else {
				acc.appendTo(tdw);
			}
		}
	}


	public void setCommand(int irun) {
		for (Clamp clamp : clamps) {
			clamp.setCommand(irun);
		}
	}


	public void setSaveInterval(double si) {
		saveInterval = si;
	}

	public double getSaveInterval() {
		return saveInterval;
	}


	public void addCurrentRecorder(String id, String tgt, LineStyle style, String channelType) {
		ChannelCurrentRecorder ccr = new ChannelCurrentRecorder(id, tgt, style, channelType);
		accessors.add(ccr);
	}

	public void addConductanceRecorder(String id, String tgt, LineStyle style, String channelType) {
		ChannelConductanceRecorder ccr = new ChannelConductanceRecorder(id, tgt, style, channelType);
		accessors.add(ccr);
	}


	public void setSeparateFiles() {
		separateFiles = true;
	}

	public void setSeparateFilesExtension(String s) {
		sfExtension = s;
	}

	public void setRecordClamps() {
		recordClamps = true;
	}

	public boolean getRecordClamps() {
		return recordClamps;
	}

}
