package org.psics.num;

import java.io.File;

import org.psics.num.model.channel.ChannelData;
import org.psics.num.model.synapse.SynapseData;
import org.psics.num.model.synapse.SypopData;
import org.psics.quantity.phys.Time;
import org.psics.util.FileUtil;
import org.psics.util.TextDataWriter;

public class PSICSCalc {



	public static CalcSummary writePPP(CompartmentTree ctree, ChannelData channelData,
			SynapseData synapseData, SypopData sypopData, Discretization disc, 
			ActivityConfig actconf, AccessConfig aconfig, RunControl runctrl,
			File fspec, boolean runJava, File flog) {


		CalcSummary csum = new CalcSummary();


		CompartmentMatrix cptmtx = new CompartmentMatrix();
		double wf = runctrl.getWeightingFactor();
		if (wf >= 0.) {
			csum.setImplicitWeightFactor(wf);
		}


		cptmtx.importCompartments(ctree.getCompartments(), ctree.getRootCompartment());

		Time dt = runctrl.getTimeStep();
		Time runtime = runctrl.getRunTime();
		int nRun = runctrl.getNRuns();
		boolean rclamp = aconfig.getRecordClamps();

		boolean obo = runctrl.advanceOneByOne();

		csum.setNRun(nRun);
		
		synapseData.setTimestep(dt);

		channelData.setTimestep(dt);
		channelData.setDeltaV(disc.getDeltaV());
		channelData.setVMin(disc.getVMin());
		channelData.setVMax(disc.getVMax());
		csum.setVDiscretization(disc);

		channelData.setDefaultThreshold(runctrl.getStochThreshold());
		channelData.setThresholds(runctrl.getStochThresholds());
		channelData.buildTables();

		double v0 = CalcUnits.getVoltageValue(runctrl.getStartPotential());

		cptmtx.setPotential(v0);
		cptmtx.allocateChannels(channelData);
		
		cptmtx.allocateSynapses(synapseData);
		
		cptmtx.summarize(csum);

		double xdt = CalcUnits.getTimeValue(dt);
		double xruntime = CalcUnits.getTimeValue(runtime);
		csum.setRuntime(xruntime);
		csum.setDt(xdt);

		boolean rclamps = aconfig.getRecordClamps();

		writePPP(xdt, xruntime, v0, wf, nRun, obo, rclamps, channelData, synapseData, sypopData, cptmtx, 
				actconf, aconfig, fspec);


		if (runJava) {
	    // JCore should read the ppp file and not be called from here
		 channelData.checkAltForms(); // this messes up the tables
         double cputime = JCore.run(xdt, xruntime, v0, wf, nRun, channelData, cptmtx, aconfig,
        		 fspec);

         FileUtil.appendLine(flog, "ppp: " + FileUtil.getRootName(fspec) + " " + cputime);
		}

		return csum;
	}




	public static void writePPP(double xdt, double xruntime, double v0, double wf,
			int nRun, boolean obo, boolean rclamps,
			ChannelData channelData, SynapseData synapseData, SypopData sypopData, CompartmentMatrix cptmtx,
			ActivityConfig actconf, AccessConfig aconfig, File fspec) {

	TextDataWriter tdw = new TextDataWriter();
	tdw.add("FPSICS-1.0");
	tdw.add(xruntime, xdt, v0, wf);
	tdw.addRowInts((obo ? 1 : 0), (rclamps ? 1 : 0));
	tdw.addMeta("runtime, timestep, v0, wf, one-by-one, record-clamps");
	tdw.addInts(nRun);
	tdw.addMeta("number of command runs");
	channelData.appendTo(tdw);
	synapseData.appendTo(tdw);
	sypopData.appendTo(tdw);
	cptmtx.appendTo(tdw, channelData.getChannelNumIDs(), sypopData.getNumIDMap());
	aconfig.appendTo(tdw);
	actconf.appendTo(tdw);
	tdw.add("END OF MODEL SPECIFICATION");
	tdw.addMeta("end marker - must start with 'END'");
	tdw.endRow();


	FileUtil.writeStringToFile(tdw.getText(), fspec);

	}




	/*
	FileFPSICS ffpsics = new FileFPSICS(fspec);
	double cputime = ffpsics.run();

	return cputime;
	// 3 return to caller to start visualization
}

*/





}
