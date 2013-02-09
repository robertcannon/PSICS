package org.psics.samples;

import java.io.File;

import org.psics.be.E;
 
import org.psics.pnative.FileFPSICS;
import org.psics.pnative.GridEngine;
import org.psics.run.PSICSModel;
import org.psics.run.Reporter;
import org.psics.samples.rallpack1.RP1;
import org.psics.samples.rallpack2.RP2;
import org.psics.samples.rallpack3.RP3;
import org.psics.samples.somaspikes.SomaSpikes;
import org.psics.util.JUtil;


public class Samples {


	static final int JAVA = 0;
	static final int FORTRAN = 1;


	public static void main(String[] argv) {

		 
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


  

	public static void stochasticSomaTests() {
		File f = new File("psics-out");
		f.mkdir();

		runTestComponent(new File(f, "soma-spikes-stochastic"), SomaSpikes.class, "run-stochastic.xml");
		runTestComponent(new File(f, "soma-spikes-continuous"), SomaSpikes.class, "run-continuous.xml");
		// runTestComponent(new File(f, "ct"), SomaSpikes.class, "run.xml");

		Reporter.mkindex(f);
	}


/*
	public static void runVCs() {
		File f = new File("psics-out");
		f.mkdir();
		runTestComponent(new File(f, "vcsteps"), VCSteps.class, "run.xml", FORTRAN);
		Reporter.mkindex(f);
	}

*/
 

 

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

 
}
