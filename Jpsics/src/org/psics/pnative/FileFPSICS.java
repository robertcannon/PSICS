package org.psics.pnative;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.psics.be.E;
import org.psics.exe.NativeRoot;
import org.psics.util.JUtil;

// runs fpsics on  a file which contains a complete specification of the run, matrix structure
// and channel tables
public class FileFPSICS {

	File fspec;

	final static int EXISTING_SHARED = 0;
    final static int JAR_SHARED = 1;
    final static int EXISTING_EXEC = 2;
    final static int JAR_EXEC = 3;

    public static int mode = 3;
    // N.B. this can be changed with command line options including -ee (existing exec)
    // which is the default when running from eclipse


	static boolean doneLoad = false;
	static boolean doneUnpack = false;

	static File fexec = null;


	private native double fpsics(String sfnm);


	public FileFPSICS(File fdir, String fnm) {
		fspec = new File(fdir, fnm + ".ppp");
	}


	public File extractNative() {
		File f = new File(System.getProperty("user.home"));
		File natfile = null;
		File fp = new File(f, ".psics");
		fp.mkdir();

		String osarch = JUtil.getOSArchitecture();
		String resdir = "";
		boolean chmod = false;
		// TODO more options for os, architecture combinations
		if (osarch.startsWith("linux")) {
			natfile = new File(fp, "fpsics");
			resdir = "linux";
			chmod = true;

		} else if (osarch.startsWith("mac")) {
			natfile = new File(fp, "fpsics");
			resdir = "mac";
			chmod = true;

		} else if (osarch.startsWith("windows")) {
			natfile = new File(fp, "fpsics.exe");
			resdir = "windows";
		} else {
			E.error("unrecognized architecture " + osarch);
		}
		File docfile = new File(fp, "mkdoc.jar");

		if (mode == JAR_EXEC) {
			try {
				JUtil.extractRelativeResource(NativeRoot.class, "mkdoc.jar", docfile);
				JUtil.extractRelativeResource(NativeRoot.class, resdir + "/" + natfile.getName(), natfile);

				if (chmod) {
					String[] ca = {"chmod", "a+x", natfile.getAbsolutePath()};
					Process p = Runtime.getRuntime().exec(ca);
					p.waitFor();
					//	FilePermission fperm = new FilePermission(natfile.getAbsolutePath(), perms);
				}

			} catch (Exception ex) {
				ex.printStackTrace();
				E.fatalError("cant extract native : " + resdir + ", " + natfile + " " + ex);
			}
		}

		if (!natfile.exists()) {
			E.fatalError("cant run native application: " + natfile.getAbsolutePath());
		}
		return natfile;
	}





	private void loadLibrary() {
		doneLoad = true;

		if (mode == EXISTING_SHARED) {
			System.loadLibrary("FPSICS");

		} else if (mode == JAR_SHARED) {
			E.fatalError("Jar shared library version temporarily disabled");
			/*
			String resname = "libFPSICS.so";
			File fnat = extractNative(resname);
			try {
				System.load(fnat.getAbsolutePath());
			} catch (Exception ex) {
				E.fatalError("cant load native library: " + ex);
				System.exit(1);
			}
			*/
		}

	}


	public double run() {
		double ret = 0.;
		if (mode == EXISTING_SHARED || mode == JAR_SHARED) {
			ret = runShared();
		} else {
			ret = runExec();
		}
		return ret;
	}


	public double runShared() {
		if (!doneLoad) {
			loadLibrary();
		}

		String sfnm = fspec.getAbsoluteFile().getPath();
		double ret = fpsics(sfnm);
		return ret;
	}


	public double runExec() {
		double ret = 0;
		if (!doneUnpack) {
			fexec = extractNative();
		}

		long t0 = System.currentTimeMillis();

		String[] ca = {fexec.getAbsolutePath(), fspec.getName()};
		E.info("native executable is at " + fexec.getAbsolutePath());
		try {
			Process p = Runtime.getRuntime().exec(ca, null, fspec.getParentFile());
			ProcessReaderThread prt = new ProcessReaderThread(p);
			prt.start();
			p.waitFor();
			prt.done();

		} catch (Exception ex) {
			E.error("exception running command: " + ex);
		}
		ret = 0.001 * (System.currentTimeMillis() - t0);
		return ret;
	}







	public static void setModeExistingExec() {
		mode = EXISTING_EXEC;
	}

}



class ProcessReaderThread extends Thread {

	BufferedReader br;
	BufferedReader bre;
	Process process;
	boolean isRunning;



	ProcessReaderThread(Process p) {
		process = p;
		br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	}


	public void done() {
		isRunning = false;
		doRead();
	}



	public void run() {
		try {
			while (isRunning) {
				Thread.sleep(50);
				doRead();
			}
		} catch (Exception ex) {
			E.warning("process reader exception: " + ex);
		}
	}

	private void doRead() {
		 try {
			 while (br.ready()) {
				 E.coreInfo(br.readLine());
			 }
			 while (bre.ready()) {
				 E.coreError(bre.readLine());
			 }
		 } catch (Exception ex) {
			 E.error("read excption for supbrocess output " + ex);
		 }
	}

}



