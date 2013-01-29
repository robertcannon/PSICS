package org.psics;

import java.io.File;

import org.psics.be.E;
import org.psics.pnative.FileFPSICS;
import org.psics.pnative.GridEngine;
import org.psics.run.PSICSImport;
import org.psics.run.PSICSModel;
import org.psics.run.Reporter;
import org.psics.samples.Samples;
import org.psics.util.FileUtil;

import org.psics.env.Version;




public class PSICS {

	static final int FORTRAN = 0;
	static final int JAVA = 1;
	int core;


	static final int ERROR = 0;
	static final int HELP = 1;
	static final int FILE = 2;
	static final int SHORT_SELF_TEST = 3;
	static final int LONG_SELF_TEST = 4;
	static final int ALL_SELF_TEST = 5;
	static final int VC_SELF_TEST = 6;
	static final int CABLES = 7;


	static final int INDEX = 30;
	static final int EXAMPLE = 20;
	int mode = ERROR;

	boolean usegrid = true; // the default is to use the gridengine if available

	String fileName;

	File nativeLib;


	public static void main(String[] argv) {
		PSICS psics = new PSICS();
		psics.processArgs(argv);
		if (psics.mode == ERROR || psics.mode == HELP) {
			printUsage();
		} else {

			psics.run();
		}
	}




	public PSICS() {

	}






	public void processArgs(String[] argv) {
		core = FORTRAN;

		for (String arg : argv) {


			if (arg.startsWith("-")) {
				if (arg.equals("-f")) {
					core = FORTRAN;
				} else if (arg.equals("-j")) {
					core = JAVA;

				} else if (arg.equals("-ee")) {
					FileFPSICS.setModeExistingExec();

				} else if (arg.startsWith("--") || arg.startsWith("-h")) {
					mode = HELP;

				} else if (arg.equals("-st")) {
					mode = SHORT_SELF_TEST;

				} else if (arg.equals("-lt")) {
					mode = LONG_SELF_TEST;

				} else if (arg.equals("-vc")) {
					mode = VC_SELF_TEST;

				} else if (arg.equals("-cables")) {
					mode = CABLES;

				} else if (arg.equals("-x")) {
					mode = INDEX;

				} else if (arg.equals("-i")) {
					usegrid = false;

				} else if (arg.equals("-e")) {
					mode = EXAMPLE;

				} else {
					E.warning("unrecognized argument: " + arg);
				}

			} else {
				fileName = arg;
				if (mode == ERROR) {
					mode = FILE;
				}
			}
		}
	}



	public void run() {
		printVersion();

		if (mode == ERROR || mode == HELP) {
			printUsage();

		} else if (mode == SHORT_SELF_TEST) {
			Samples.runShort();

		} else if (mode == LONG_SELF_TEST) {
			Samples.runLong();

		} else if (mode == ALL_SELF_TEST) {
			Samples.runAll();

		} else if (mode == CABLES) {
			Samples.runCables();

		} else if (mode == EXAMPLE) {
			Samples.runExample(fileName);

		} else if (mode == INDEX) {
			String ud = System.getProperty("user.dir");
			File fdir = new File(ud);
			Reporter.mkindex(fdir);

		} else if (mode == FILE) {
			File f = new File(fileName);
				if (f.exists()) {
					String s = FileUtil.readNLinesFromFile(f, 5);
					if (s.indexOf("<neuroml ") >= 0) {
						PSICSImport psim = new PSICSImport(f);
						psim.convert();


					} else {
					PSICSModel pc = new PSICSModel(f);
					if (mode == JAVA) {
						pc.runJava();

					} else {
						if (usegrid && GridEngine.isAvailable()) {
							pc.gridSubmit();

						} else {
						  pc.runFortran();

					}
					}
					}

				} else {
					E.error("No such file - " + f.getAbsolutePath());
				}
		}
	}







	public static void printUsage() {
		 String s = "USAGE: PSICS [-f|-j] [-x|-i] [-st|-lt|file] \n" +
				    "-j        use java reference core rather than fortran\n" +
				    "-ee       use existing executable rather than the one in the jar file\n" +
				    "-st       run short internal test models\n" +
				    "-lt       run a longer series of internal test models\n" +
				    "-x        build and index of any results in the current folder\n" +
				    "-i        interactive mode: don't check for a grid to parallelize on\n" +
				    "file      when the file argument is specifie PSICS runs the \n" +
				    "          model defined in the file, (and othere files referred \n" +
				    "          to by its contents). The file should contain XML \n" +
				    "          specifying a single a PSICSRun element as described \n" +
				    "          at http://wwww.psics.org/formats/\n" +
				    "For example:\n" +
				    "   psics model.xml         run the model defined in model.xml\n" +
				    "   psics -h  (or --help)   show help (this mesage)\n" +
				    "   psics -st               run the short test with an internal model    \n" +
				    "   psics neuromlmodel.xml  import a neuroml file making the nearest psics equivalent\n" +
				    "                           warnings may be issued about aspects requiring manual editing\n" +
				    "                           these should be fixed before running a model\n";
		 System.out.println(s);
	}


	@SuppressWarnings("unused")
	public static void qualifiedMain(String arg, String[] argv) {
   	    String[] qargv = new String[argv.length + 1];
    	for (int i = 0; i < argv.length; i++) {
		    qargv[i+1] = argv[i];
		    qargv[0] = "-f";
	    }
	PSICS.main(qargv);
	}


	public boolean checkNative() {
		boolean ret = false;
		try {
			System.loadLibrary("FPSICS");
			ret = true;
		} catch (Exception ex) {

		}
		return ret;
	}


	private void printVersion() {
		E.log("PSICS version " + Version.getVersionName() + " (" + Version.getVersionDate() + ")");
	}


}
