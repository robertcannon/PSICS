package org.psics;

import java.io.File;

import org.psics.be.E;
//import org.psics.examples.chowwhite.ChowWhite;
//import org.psics.examples.cianmar30.CM30;
//import org.psics.examples.location.CellLoc;
//import org.psics.examples.mainen.Mainen;
import org.psics.examples.chowwhite.ChowWhite;
import org.psics.examples.cianmar30.CM30;
import org.psics.examples.cwvclamp.CWVclamp;
import org.psics.examples.location.CellLoc;
import org.psics.examples.mainen.Mainen;
import org.psics.examples.manychannels.ManyChannels;
import org.psics.examples.meanvariance.MeanVariance;
import org.psics.examples.migca1.MigCA1;
import org.psics.examples.minor.Minor;
import org.psics.examples.multirec.MultiRec;
import org.psics.examples.onsurface.OnSurface;
import org.psics.examples.p18_robert.p18;
import org.psics.examples.params.Params;
import org.psics.examples.psd.PSD;
//import org.psics.examples.migca1.MigCA1;
//import org.psics.examples.multirec.MultiRec;
//import org.psics.examples.psd.PSD;
import org.psics.examples.rallpack1.RP1;
import org.psics.examples.rallpack2.RP2;
import org.psics.examples.rallpack3.RP3;
import org.psics.examples.rallpack3stoch.RP3stoch;
import org.psics.examples.raster.RasterEx;
import org.psics.examples.resonance.Resonance;
//import org.psics.examples.rallpack3stoch.RP3stoch;
//import org.psics.examples.raster.RasterEx;
//import org.psics.examples.smartrec.SmartRec;
import org.psics.examples.singlecpt.SingleCpt;
import org.psics.examples.smalldt.SmallDt;
import org.psics.examples.smartrec.SmartRec;
import org.psics.examples.somaspikes.SomaSpikes;
import org.psics.examples.stimtest.StimTest;
import org.psics.examples.stochdet.StochDet;
import org.psics.examples.synapticstim.SynapticStim;
import org.psics.examples.vcsteps.VCSteps;
import org.psics.pnative.FileFPSICS;
import org.psics.pnative.GridEngine;
//import org.psics.run.PSICSImport;
import org.psics.run.PSICSModel;
import org.psics.run.Reporter;
import org.psics.util.JUtil;


public class WkTest {


	static final int JAVA = 0;
	static final int FORTRAN = 1;


	public static void main(String[] argv) {

		FileFPSICS.setModeExistingExec();
 
			File f = new File("psics-out");
			f.mkdir();
		 
			// runTestComponent(new File(f, "singlecpt"), SingleCpt.class, "run-continuous.xml");
			// runTestComponent(new File(f, "resonance"), Resonance.class, "run-sr.xml");
			
			 runTestComponent(new File(f, "manychannels"), ManyChannels.class, "run-mc.xml");
			
			E.info("reindexing...");
			Reporter.mkindex(f);
		 
	    E.info("PSICS finished");
	}



  

 
     

	public static void runExample(String exname) {
		File f = new File("psics-out");
		f.mkdir();

		try {
			Class<?> c = Class.forName("org.psics.examples." + exname + ".Root");

			runTestComponent(new File(f, exname), c, "run.xml");

			Reporter.mkindex(f);
		} catch (Exception ex) {
			E.error("cant find " + exname + " in classpath");
		}
	}

	private static void runJavaTestComponent(File dir, Class<?> root, String modelName) {
		runTestComponent(dir, root, modelName, JAVA);
    }

	private static void runTestComponent(File dir, Class<?> root, String modelName) {
			runTestComponent(dir, root, modelName, FORTRAN);
	}

	private static void runTestComponent(File dir, Class<?> root, String modelName, int mode) {

		E.info("output going to " + dir);

		dir.mkdir();
		PSICSModel pm = new PSICSModel(root, modelName);
		pm.setDestinationFolder(dir);
		JUtil.extractStaticFieldResources(root, dir);

		if (mode == JAVA) {
			pm.runJava();

		} else if (GridEngine.isAvailable()) {
			pm.gridSubmit();
			// also submits the mkdocs job conditional on the rest finishing.

		} else {
			pm.runFortran();
		}
	}

 
}
