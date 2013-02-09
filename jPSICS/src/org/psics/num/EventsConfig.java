package org.psics.num;

import org.psics.be.E;
import org.psics.util.TextDataWriter;


public class EventsConfig {

	
    public final static int UNIFORM = 1;
    public final static int POISSON = 2;
    public final static int EXPLICIT = 10;
    public final static int THRESHOLD = 20;
	
    int type;
	int popnumid;
	double frequency = 0.;
	int seed = -1;
	double threshold = 0.; // only used if this is a threshold sensor
	
	double[] times;
	int[] indices;
	
	public EventsConfig(int ty) {
		type = ty;
	}



	public void setTargetPopID(Integer nid) {
		popnumid = nid;		
	}


	public void setFrequency(double fv) {
		frequency = fv;
	}

	public void setThreshold(double t) {
		threshold = t;
	}
	

	public void setSeed(int sd) {
		seed = sd;
	}


	public void appendTo(TextDataWriter tdw) {
		tdw.addInts(type, popnumid, seed);
		tdw.addMeta("type code, population no, seed");
		if (type == THRESHOLD) {
			tdw.add(threshold);
			tdw.addMeta("threshold");
		} else if (type == UNIFORM || type == POISSON){
			tdw.add(frequency);
			tdw.addMeta("frequency");
		} else if (type == EXPLICIT) {
			tdw.addInts(times.length);
			tdw.addMeta("number of explicit events");
			tdw.newRow();
			for (int i = 0; i < times.length; i++) {
				tdw.addFormattedRow(String.format(" %12.6g %6d", times[i], indices[i]));
			}
			
		} else {
			
			E.error("unrecognized type " + type);
		}
	}



	public void setData(double[] ts, int[] inds) {
		times = ts;
		indices = inds;
	}

	
}
