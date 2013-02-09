package org.psics.model.activity;

import java.io.File;
import java.util.StringTokenizer;

import org.psics.be.DataFileSourced;
import org.psics.be.E;
import org.psics.num.EventsConfig;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.util.FileUtil;
import org.psics.quantity.annotation.ReferenceToFile;

@ModelType(info = "A sequence of events read from an external file. The " +
		"file should contain two columns with times and synapse indexes. " +
		"The synapse indexes refer to the index of a synapse in the target " +
		"population starting at 0.", 
		standalone = false, tag = "", usedWithin = { AfferentEvents.class })
public class EventSequence extends EventSource implements DataFileSourced {
	
	@ReferenceToFile(tag = "Name of the file containing the input events", fallback = "", required=true)
	public String file;

	double[] times;
	int[] indices;
	
	
	
	public String getFileName() {
		return file;
	}
	

 
	public void setData(double[][] da) {
		int nt = da.length;
		times = new double[nt];
		indices = new int[nt];
		for (int i = 0; i < nt; i++) {
			double[] lin = da[i];
			times[i] = lin[0];
			indices[i] = (int)Math.round(lin[1]);
		}
		E.info("read " + nt + " events into EventSequence");
	}
 
	
	public EventsConfig getEventsConfig() {
		EventsConfig ret = new EventsConfig(EventsConfig.EXPLICIT);
		ret.setData(times, indices);
		return ret;    
	}
	
	
	private EventsConfig readOwnFile() {
		// NB This shouldn't be used - we implement data file sourced, so that should do everything 
		// for us
		File fst = new File(file);
		E.info("about to read " + fst.getAbsolutePath());
		String s = FileUtil.readStringFromFile(fst);
		s = s.trim();
		StringTokenizer st = new StringTokenizer(s, "\n");
		int nt = st.countTokens();
		double[] times = new double[nt];
		int[] indices = new int[nt];
		int i = 0;
		while (st.hasMoreTokens()) {
			String lin = st.nextToken();
			StringTokenizer sst = new StringTokenizer(lin, " ");
			times[i] = Double.parseDouble(sst.nextToken());
			indices[i] = (int)(Math.round(Double.parseDouble(sst.nextToken())));
			i += 1;
		}
		EventsConfig ret = new EventsConfig(EventsConfig.EXPLICIT);
		ret.setData(times, indices);
		return ret;
	}



}
