package org.psics;

import java.io.File;

import org.psics.be.E;
//import org.psics.examples.chowwhite.ChowWhite;
//import org.psics.examples.cianmar30.CM30;
//import org.psics.examples.location.CellLoc;
//import org.psics.examples.mainen.Mainen;
import org.psics.examples.cwvclamp.CWVclamp;
import org.psics.examples.location.CellLoc;
import org.psics.examples.meanvariance.MeanVariance;
import org.psics.examples.migca1.MigCA1;
import org.psics.examples.minor.Minor;
import org.psics.examples.onsurface.OnSurface;
import org.psics.examples.p18_robert.p18;
import org.psics.examples.params.Params;
//import org.psics.examples.migca1.MigCA1;
//import org.psics.examples.multirec.MultiRec;
//import org.psics.examples.psd.PSD;
import org.psics.examples.rallpack1.RP1;
import org.psics.examples.rallpack2.RP2;
import org.psics.examples.rallpack3.RP3;
//import org.psics.examples.rallpack3stoch.RP3stoch;
//import org.psics.examples.raster.RasterEx;
//import org.psics.examples.smartrec.SmartRec;
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


public class SelfTest {


	static final int JAVA = 0;
	static final int FORTRAN = 1;


	public static void main(String[] argv) {

		FileFPSICS.setModeExistingExec();

		if (argv.length == 1 && argv[0].equals("-all")) {
			runAll();

		} else if (false) {
			stochasticSomaTests();

		} else {
			File f = new File("psics-out");
			f.mkdir();
			runTestComponent(new File(f, "p18"), p18.class, "run_distrib.xml");
			
		//	runTestComponent(new File(f, "cell-loc"), CellLoc.class, "run.xml");
 
		// runTestComponent(new File(f, "synapses"), SynapticStim.class, "run.xml");

//			runTestComponent(new File(f, "minor"), Minor.class, "CA1PyramidalCell.xml");
 
			
		//	runTestComponent(new File(f, "params"), Params.class, "run.xml");

			
			// runTestComponent(new File(f, "minor"), Minor.class, "CA1PyramidalCell.xml");
 
//			runTestComponent(new File(f, "smalldt"), SmallDt.class, "CA1PyramidalCell.xml");
			
			
			// runTestComponent(new File(f, "onsurface"), OnSurface.class, "run.xml");
			
		//	runTestComponent(new File(f, "migliore-1a"), MigCA1.class, "run1a.xml");

			// runTestComponent(new File(f, "rallpack3"), RP3.class, "run.xml");
			
//			runTestComponent(new File(f, "cwvclamp"), CWVclamp.class, "rcwvc.xml");
//			runTestComponent(new File(f, "vcsteps"), VCSteps.class, "run.xml");

			
			
 
//			runTestComponent(new File(f, "rallpack1"), RP1.class, "run.xml");
		//	runTestComponent(new File(f, "synapses"), SynapticStim.class, "run.xml");
 
			

			// runTestComponent(new File(f, "smartrec"), SmartRec.class, "run.xml");

			
//			runTestComponent(new File(f, "ivconv"), MeanVariance.class, "runiv.xml");
//			runTestComponent(new File(f, "ivconv"), MeanVariance.class, "runiv-mstoch.xml");
//			runTestComponent(new File(f, "mean-variance-iv-stoch"), MeanVariance.class, "runivstoch.xml");

			// runTestComponent(new File(f, "stochdet"), StochDet.class, "run.xml");

			//runTestComponent(new File(f, "channel-functions"), MigCA1.class, "runfunc.xml");

			// runTestComponent(new File(f, "stimtest"), StimTest.class, "run.xml");

			/*
			runTestComponent(new File(f, "functest"), MigCA1.class, "runfunc.xml");
			runTestComponent(new File(f, "migliore-pass"), MigCA1.class, "runpass.xml");
			runTestComponent(new File(f, "psd"), PSD.class, "run.xml");
			runTestComponent(new File(f, "cianmar30"), CM30.class, "run.xml");
			runTestComponent(new File(f, "raster"), RasterEx.class, "run.xml");
			runTestComponent(new File(f, "mean-variance"), MeanVariance.class, "run.xml");
			runTestComponent(new File(f, "rallpack1"), RP1.class, "run.xml");
			runTestComponent(new File(f, "rallpack2"), RP2.class, "run.xml");
			runTestComponent(new File(f, "rallpack3-stoch"), RP3stoch.class, "run-stoch.xml");
			runTestComponent(new File(f, "rallpack3-stoch2"), RP3stoch.class, "run-stoch2.xml");
			runTestComponent(new File(f, "chowwhite"), ChowWhite.class, "run-chowwhite-varysize.xml");
			runTestComponent(new File(f, "multirec"), MultiRec.class, "run.xml");

			runTestComponent(new File(f, "mainen"), Mainen.class, "run.xml");
			*/



			/*

*/

			// runTestImport(new File(f, "neuromlimport"), NMLEx.class, "MossyCellBiophys.xml");
			// runTestComponent(new File(f, "cianmar30"), CM30.class, "run.xml");
		//	 runTestComponent(new File(f, "cwvclamp"), CWVclamp.class, "rcwvc.xml");
	//	runTestComponent(new File(f, "vcsteps"), VCSteps.class, "run.xml");

			E.info("reindexing...");
			Reporter.mkindex(f);
		}
	    E.info("PSICS finished");
	}



	public void runall() {
		validationTests();
		stochasticSomaTests();
	}


	public static void runShort() {
		stochasticSomaTests();
		 E.info("PSICS finished");
	}



	public static void runLong() {
		validationTests();
		stochasticSomaTests();
		E.info("PSICS finished");
	}


	public static void runAll() {
		validationTests();
		stochasticSomaTests();
		E.info("PSICS finished");
	}

	public static void runCables() {
		E.missing("need some cable tests...");
		E.info("PSICS finished");
	}



	public static void paper1Data() {
		File f = new File("psics-out");
		f.mkdir();
	//	runTestComponent(new File(f, "p1-f1-mean-variance"), MeanVariance.class, "run.xml");
	//	runTestComponent(new File(f, "p1-f2-rp1-det"), RP1.class, "run.xml");
	//	runTestComponent(new File(f, "p1-f2-rp2-det"), RP2.class, "run.xml");
		runTestComponent(new File(f, "p1-f2-rp3-det"), RP3.class, "run.xml");
	//	runTestComponent(new File(f, "p1-f2-rp1-stoch"), RP1.class, "run-stoch.xml");
	//	runTestComponent(new File(f, "p1-f2-rp2-stoch"), RP2.class, "run-stoch.xml");
	//	runTestComponent(new File(f, "p1-f2-rp3-stoch"), RP3.class, "run-stoch.xml");
	//	runTestComponent(new File(f, "p1-f3-1a-det"), MigCA1.class, "run1a.xml");
	//	runTestComponent(new File(f, "p1-f3-1c-det"), MigCA1.class, "run1c.xml");
	//	runTestComponent(new File(f, "p1-f3-1a-stoch"), MigCA1.class, "run1a-stoch.xml");

	//  runTestComponent(new File(f, "cmtest"), MigCA1.class, "runcmtest.xml");
		Reporter.mkindex(f);
	}




	public static void stochasticSomaTests() {
		File f = new File("psics-out");
		f.mkdir();

		runTestComponent(new File(f, "soma-spikes-stochastic"), SomaSpikes.class, "run-stochastic.xml");
		runTestComponent(new File(f, "soma-spikes-continuous"), SomaSpikes.class, "run-continuous.xml");
		// runTestComponent(new File(f, "ct"), SomaSpikes.class, "run.xml");

		Reporter.mkindex(f);
	}



	public static void runVCs() {
		File f = new File("psics-out");
		f.mkdir();
		runTestComponent(new File(f, "vcsteps"), VCSteps.class, "run.xml", FORTRAN);
		Reporter.mkindex(f);
	}




	public static void meanVar() {
		File f = new File("psics-out");
		f.mkdir();
		runTestComponent(new File(f, "p1-f1-mean-variance"), MeanVariance.class, "run.xml");
		Reporter.mkindex(f);
	}

	public static void rp1() {
		File f = new File("psics-out");
		runTestComponent(new File(f, "rallpack1"), RP1.class, "run.xml");
		runTestComponent(new File(f, "rallpack1-IE"), RP1.class, "run-ie.xml");
		Reporter.mkindex(f);
	}

	public static void rp2() {
		File f = new File("psics-out");
		runTestComponent(new File(f, "rallpack2"), RP2.class, "run.xml");
		runTestComponent(new File(f, "rallpack2-IE"), RP2.class, "run-ie.xml");
		Reporter.mkindex(f);
	}



	public static void rp3() {
		File f = new File("psics-out");
		runTestComponent(new File(f, "rallpack3"), RP3.class, "run.xml");

		Reporter.mkindex(f);
	}

	public static void rp3J() {
		File f = new File("psics-out");
		runJavaTestComponent(new File(f, "rallpack3"), RP3.class, "run.xml");

		Reporter.mkindex(f);
	}


	public static void validationTests() {
		File f = new File("psics-out");
		f.mkdir();

		runTestComponent(new File(f, "rallpack1"), RP1.class, "run.xml");
		runTestComponent(new File(f, "rallpack2"), RP2.class, "run.xml");
		runTestComponent(new File(f, "rallpack3"), RP3.class, "run.xml");

		Reporter.mkindex(f);
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


	/*
	private static void runTestImport(File dir, Class<?> root, String modelName) {

		dir.mkdir();
		PSICSImport pm = new PSICSImport(root, modelName);
		pm.setDestinationFolder(dir);
		pm.convert();
	}




	private static void runFile(File f) {
		PSICSModel pc = new PSICSModel(f);
		pc.runFortran();
		// Reporter.mkdocs(pc.getOutputFolder());
	}
*/
}
