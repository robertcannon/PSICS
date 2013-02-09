package org.psics.run;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.psics.be.E;
import org.psics.distrib.ChannelBalance;
import org.psics.distrib.PointTree;
import org.psics.distrib.DistribSpec;
import org.psics.model.channel.KSChannel;
import org.psics.model.control.CommandConfig;
import org.psics.model.control.PSICSRun;
import org.psics.model.control.RunConfig;
import org.psics.model.control.RunSet;
import org.psics.model.control.StructureDiscretization;
import org.psics.model.display.ViewConfig;
import org.psics.model.electrical.CellProperties;
import org.psics.model.environment.CellEnvironment;
import org.psics.model.morph.CellMorphology;
import org.psics.morph.LocalDiscretizationData;
import org.psics.morph.MergeDiscretizer;
import org.psics.morph.TreePoint;
import org.psics.num.AccessConfig;
import org.psics.num.ActivityConfig;
import org.psics.num.CalcSummary;
import org.psics.num.CalcUnits;
import org.psics.num.ChannelGE;
import org.psics.num.CompartmentTree;
import org.psics.num.Discretization;
import org.psics.num.PSICSCalc;
import org.psics.num.RunControl;
import org.psics.num.math.MersenneTwister;
import org.psics.num.model.channel.ChannelData;
import org.psics.num.model.channel.TableChannel;
import org.psics.num.model.channel.TransitionTypes;
import org.psics.num.model.synapse.SynapseData;
import org.psics.num.model.synapse.SypopData;
import org.psics.num.model.synapse.TableSynapse;
import org.psics.om.Serializer;
import org.psics.out.ResultsWriter;
import org.psics.pnative.FileFPSICS;
import org.psics.quantity.phys.Voltage;
import org.psics.read.FileModelSource;
import org.psics.read.ModelSource;
import org.psics.read.ResourceModelSource;
import org.psics.util.FileUtil;
import org.psics.write.ModelTextalizer;
import org.psics.model.synapse.Synapse;

import org.psics.quickxml.ElementExtractor;

public class PSICSModel {

	File rootFile;
	File outFolder;

	Class<?> rootClass;
	String rootName;

	ModelSource mSource;

	PSICSRun psicsRun;



	double cputime = 0.;


	public PSICSModel(File f) {
		rootName = f.getName();
		if (!f.exists()) {
			E.error("no such file " + f);
		} else {
			rootFile = f;
			mSource = new FileModelSource(f);

			String fsrc = FileUtil.readStringFromFile(f);
			String sof = ElementExtractor.getAttribute("outputFolder", fsrc);
			if (sof != null) {
				sof = sof.trim();
			}

			if (sof != null && sof.length() > 0) {
				if (sof.startsWith("/")) {
					outFolder = new File(sof);
				} else {
					outFolder = new File(f.getParentFile(), sof);
				}
			} else {
				outFolder = FileUtil.extensionSibling(f, "-results");
			}

			if (!outFolder.exists()) {
				outFolder.mkdir();
			}
		}
	}


	public PSICSModel(Class<?> cls, String rnm) {
		rootClass = cls;
		rootName = rnm;
		rootFile = null;
		outFolder = null;
		mSource = new ResourceModelSource(cls, rnm);
	}


	public void setDestinationFolder(File f) {
		outFolder = f;
		mSource.setCCDir(f);
	}


	public double getCPUTime() {
		return cputime;
	}




	public void gridSubmit() {
		File flog = new File(outFolder, "log.txt");
		FileUtil.clearNew(flog);
		E.log("building ppp files (grid engine)");
		ArrayList<String> pppAL = buildPPPs(false, null);
		FileUtil.appendLine(flog, rootName +   " items=" + pppAL.size() + " " + timestamp());

		FileFPSICS ffp = new FileFPSICS(outFolder, "");
		File fexec = ffp.extractNative();


		StringBuffer sb = new StringBuffer();
		sb.append("#!/bin/bash\n");
		sb.append("#$ -S /bin/bash\n");
		String savedir = outFolder.getAbsolutePath();
		sb.append("savedir=" + savedir + "\n");
		sb.append("wkdir=${TMPDIR:-" + savedir + "}\n");

		sb.append("cd " + outFolder.getAbsolutePath() + "\n");
		int ijo = 1;
		for (String s : pppAL) {
			ijo += 1;
			sb.append("ppp[" + ijo + "]=" + s + "\n");
		}
		sb.append("if [ ${SGE_TASK_ID} -eq 1 ]\n");
		sb.append("then\n");
		sb.append("cd " + savedir + "\n");
		File fdocjar = new File(fexec.getParentFile(), "mkdoc.jar");

		sb.append("   if [ -f ~/enablejava.sh ]; then\n");
		sb.append("       source ~/enablejava.sh\n");
		sb.append("   fi\n");

		sb.append("   java -jar " + fdocjar.getAbsolutePath() + "\n");
		sb.append("else\n");
		sb.append("   cd $wkdir\n");
		sb.append("   if [ \"$wkdir\" != \"$savedir\" ]; then\n");
		sb.append("       cp ${savedir}/${ppp[${SGE_TASK_ID}]}.ppp .\n");
		sb.append("       touch log.txt\n");
		sb.append("   fi\n");
		sb.append("   " + fexec.getAbsolutePath() + " ${ppp[${SGE_TASK_ID}]}.ppp\n");
		sb.append("   if [ \"$wkdir\" != \"$savedir\" ]; then\n");
		sb.append("       cp ${ppp[${SGE_TASK_ID}]}.* ${savedir}\n");
		sb.append("       cat log.txt >> ${savedir}/log.txt\n");
		sb.append("   fi\n");


		sb.append("fi\n");

		File fscript =  new File(outFolder, "run-ge.sh");
		FileUtil.writeStringToFile(sb.toString(),  fscript);
		E.info("written grid engine script to " + fscript.getAbsolutePath());


		String hostname = "localhost";
		try {
		        InetAddress addr = InetAddress.getLocalHost();
		        hostname = addr.getHostName();
		} catch (Exception e) {
		    	E.warning("cant get hostname of local machine? " + e);
		}
		String pth = fscript.getAbsolutePath();


		// now we have an array job to submit to run the ppp files, and
		// a documentation job for after the array has finished to be run on the machine
		// we're currently on (since we know we have java and it is quick).
		String subcmd1 = "qsub -t 2-" + ijo + " " + pth;

		// maybe this host isn't taking jobs though - run on any with enablejava.sh called
		// if available
		// String subcmd2 = "qsub -t 1 -l hostname=" + hostname + " -hold_jid  DEPID " + pth;
		String subcmd2 = "qsub -t 1 -hold_jid  DEPID " + pth;


		try {
			E.info("executing " + subcmd1);
			Process p = Runtime.getRuntime().exec(subcmd1);
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			p.waitFor();

			int job1id = -1;
			String ssub = br.readLine();
			E.info(ssub);
			int ioff = ssub.indexOf("job-array");
			if (ioff > 0) {
				String sr = ssub.substring(ioff+9, ssub.length());
				int idot = sr.indexOf(".");
				if (idot > 0 && idot < 12) {
					job1id = Integer.parseInt(sr.substring(0, idot).trim());
				}
			}

			if (job1id > 0) {
				subcmd2 = subcmd2.replace("DEPID", "" + job1id);


			} else {
				E.error("cant get job id from " + ssub);
			}
			String lin;
			while ((lin = br.readLine()) != null) {
				E.info(lin);
			}

			E.info("executing " + subcmd2);
		    Runtime.getRuntime().exec(subcmd2);
			BufferedReader br2 = new BufferedReader(new InputStreamReader(p.getInputStream()));

			while ((lin = br2.readLine()) != null) {
				E.info(lin);
			}

		} catch (Exception ex) {
			E.error("failed to submit jobs correctly: " + ex);
		}
	}

	@SuppressWarnings("unused")
	public void runFortran() {
		File flog = new File(outFolder, "log.txt");
		FileUtil.clearNew(flog);
		FileUtil.appendLine(flog, rootName + " " + timestamp());
		E.log("building ppp files");
		ArrayList<String> pppAL = buildPPPs(false, flog);

		for (String s : pppAL) {
			FileFPSICS ffp = new FileFPSICS(outFolder, s);
			E.log("running fpsics on " + s + ".ppp");
			double cpt = ffp.run();
			// FileUtil.appendLine(flog, "ppp: " + s + " " + cpt);
		}

		for (RunSet rs : psicsRun.getRunSets()) {
			if (rs.mergeScan()) {
				ScanMerger sm = new ScanMerger(outFolder, rs.getFileRoots(), rs.getMergedRoot());
				sm.merge();
			}
		}

		Reporter.mkdocs(outFolder);
	}





	public void runJava() {
		File flog = new File(outFolder, "log.txt");
		FileUtil.clearNew(flog);
		FileUtil.appendLine(flog, rootName + " " + timestamp());
		ArrayList<String> pppAL = buildPPPs(true, flog);

		 Reporter.mkdocs(outFolder);
	}




	  public static String timestamp() {
		     SimpleDateFormat f;
		     f = new SimpleDateFormat("HH:mm:ss  EEE d MMM yyyy");
		     return f.format(new Date());
		   }





	public String getInfo() {
		if (psicsRun == null) {
			readModel();
		}
		return psicsRun.getInfo();
	}



	public String getTextVersion() {
		if (psicsRun == null) {
			readModel();
		}
		return psicsRun.getTextVersion(new ModelTextalizer());
	}


	public ArrayList<String> getInfoParas() {
		if (psicsRun == null) {
			readModel();
		}
		return psicsRun.getInfoParas();
	}



	private void readModel() {
		readModel(true);
	}

	private void readModel(boolean bresolve) {
		Object obj = mSource.read(bresolve);
		if (! (obj instanceof PSICSRun)) {
			E.error("Wrong type of root object - must be of type PSICSRun, not " + obj);
			return;
		}



		psicsRun = (PSICSRun)obj;
	}



	private void checkMakeOutputFolder() {
		if (outFolder == null) {
			outFolder = FileUtil.extensionSibling(new File(rootName), "-results");
			outFolder.mkdirs();
			E.info("made output folder " + outFolder);
		}
	}


	public File getOutputFolder() {
		checkMakeOutputFolder();
		return outFolder;
	}

	public File getRootFile() {
		return rootFile;
	}




	private ArrayList<String> buildPPPs(boolean runJava, File flog) {
		ArrayList<String> pppAL = new ArrayList<String>();

		// E.log("PSICS started " + TimeUtil.timeDateStamp());
		checkMakeOutputFolder();

		mSource.setCCDir(outFolder);
		if (psicsRun == null) {
			readModel();
		}
		psicsRun.resolve();

		ArrayList<CommandConfig> emptyacc = new ArrayList<CommandConfig>();
		emptyacc.add(null);

		if (psicsRun.isMultiRun()) {
			for (RunSet runset : psicsRun.getRunSets()) {
				String fpat = runset.getFilePattern();
				// E.info("processing run set " + runset.getRunConfigs().size());

				for (RunConfig rc : runset.getRunConfigs()) {

					rc.applyTo(psicsRun);
					String wkfpat = fpat.replace("$", rc.getValueString());

					CalcSummary cs = null;
					if (rc.nCommands() == 0) {
						cs = buildCommandsPPP(psicsRun, wkfpat, emptyacc, runJava, flog);
					} else {
						cs = buildCommandsPPP(psicsRun, wkfpat, rc.getCommandConfigs(), runJava, flog);
					}
					pppAL.add(wkfpat);
					rc.summarize(cs);

					String scs = Serializer.serialize(cs);
					FileUtil.writeStringToFile(scs, new File(outFolder, wkfpat + ".sum"));
				}
			}

		} else {
			String fpat = "psics-out"; // TODO better choice of default pattern
			CalcSummary cs = buildCommandsPPP(psicsRun, fpat, emptyacc, runJava, flog);
			String scs = Serializer.serialize(cs);
			FileUtil.writeStringToFile(scs, new File(outFolder, fpat + ".sum"));
			pppAL.add(fpat);

		}

	//	String s = Serializer.serialize(css);
	//	FileUtil.writeStringToFile(s, new File(outFolder, "summary.xml"));
		return pppAL;
	}





	public CalcSummary buildCommandsPPP(PSICSRun psicsR, String fnm, ArrayList<CommandConfig> commands,
			boolean runJava, File flog) {

		File runRoot = new File(outFolder, fnm);

		ResultsWriter rw = new ResultsWriter(runRoot, "", ResultsWriter.OUT);
		rw.initMagic("cctdif2d");

		File fppp = rw.getSiblingFile("", "ppp");

		// E.info("ppp file is " + fppp.getName() + " from " + rw.getFile().getName() + " " + runRoot);

		CellMorphology cm = psicsR.getCellMorphology();

		boolean squareCaps = (cm.getSquareCaps() || psicsR.getSquareCaps());
		
		TreePoint[] allTPs = cm.exportTreePoints(squareCaps);
		PointTree pointTree = new PointTree(allTPs);
 
		StructureDiscretization sd = psicsR.getStructureDiscretization();
		double esize = sd.getElementSize();
		for (LocalDiscretizationData ldd : sd.getLocalDiscs()) {
			pointTree.applyLocalDiscretization(ldd);
		}
		
		// remesh the cell
		MergeDiscretizer md = new MergeDiscretizer(pointTree.getPoints());

		
		boolean pp = sd.getNoGroups();
	 
		int maxnp = psicsR.getMaxPoints();
		double interpPower = -0.5; // TODO what should it be?
		CompartmentTree ctree = md.getCompartmentTree(esize, pp, squareCaps, maxnp, interpPower);

		rw.writeString(ctree.getMeshAsText());

		// allocate channels to compartments
		CellProperties cdist = psicsR.getCellProperties();
		ctree.setMembraneCapacitance(cdist.getMembraneCapacitance());
		ctree.setBulkRestivitity(cdist.getBulkResistivity());
		
		DistribSpec channelDistSpec = cdist.getChannelDistributionSpecification();
		DistribSpec synapseDistSpec = cdist.getSynapseDistributionSpecification();
		
		
		CellEnvironment cellenv = psicsR.getCellEnvironment();

		TransitionTypes.setTemperature(cellenv.getTemperature());


		MersenneTwister mersenne = new MersenneTwister();

		
		channelDistSpec.passiveInit(pointTree, ctree);
		
		// TODO - if fully continuous, can do this anyway? May not resolve sub-compartment boundaries though?
		if (psicsR.populateByCompartments()) {
			// old method - doesn't allocate exact channel positions
			channelDistSpec.populateCompartments(ctree, mersenne);
			if (synapseDistSpec.hasSynapses()) {
				E.missing("cant do compartment based allocation for synapses");
			}
		} else {
			channelDistSpec.populate(pointTree, ctree, mersenne, squareCaps, DistribSpec.CHANNEL);
			synapseDistSpec.populate(pointTree, ctree, mersenne, squareCaps, DistribSpec.SYNAPSE);
		}

		
		ChannelData channelData = new ChannelData();
		channelData.setTemperature(cellenv.getTemperature());

		
		for (KSChannel ksc : cdist.getKSChannels()) {
			TableChannel tch = ksc.tablifyAsSingleComplex();

			String pid = ksc.getPermeantIonID();
			Voltage vrev = cellenv.getDefaultReversal(pid);

			tch.setDefaultReversal(vrev);
			channelData.addTableChannel(tch);

			if (ksc.isMultiComplex()) {
				TableChannel tchmc = ksc.tablifyMultiComplex();
				tchmc.setDefaultReversal(vrev);
				tch.setAlt(tchmc);
				tchmc.setAlt(tch);
				channelData.addTableChannel(tchmc);

			}
		}
		channelData.fixOrder();

		
		
		SynapseData synapseData = new SynapseData();
		synapseData.setTemperature(cellenv.getTemperature());
		
		
		for (Synapse syn : cdist.getSynapses()) {
			TableSynapse tsy = syn.tablify();
			String pid = syn.getPermeantIonID();
			Voltage vrev = cellenv.getDefaultReversal(pid);

			tsy.setDefaultReversal(vrev);
			synapseData.addTableSynapse(tsy);
		}
		synapseData.fixOrder();
		
		SypopData sypopData = synapseDistSpec.getSypopData(synapseData.getSynapseNumIDs());
		
		
		for (ChannelBalance cb : channelDistSpec.getChannelBalances()) {
			HashMap<String, ChannelGE> geHM = null;
			geHM = channelData.getGEHM(cb.getPotential(), cellenv.getTemperature());
			cb.applyTo(ctree, geHM, mersenne);
		}

		RunControl runctrl = psicsR.getRunControl();
		Discretization disc = psicsR.getDiscretization();
		AccessConfig aconfig = psicsR.getAccessConfig(commands, ctree);
		int nrep = psicsR.getNRepeat();
		if (commands.size() > 1) {
			runctrl.setNRuns(commands.size());
			if (nrep > 1) {
				E.warning("both multiple commands and repeat > 0 supplied: ignoring repeat");
			}
		} else {
			runctrl.setNRuns(nrep);
		}
		
		aconfig.attachTo(ctree, channelData);

		ActivityConfig actconf = psicsR.getActivityConfig(sypopData.getNumIDMap());
		
		
		String smsg = "" + fnm + ": " + ctree.size() + " cpts, " + ctree.summarizeChannels();
		smsg += " dt=" + CalcUnits.getTimeValue(runctrl.getTimeStep()) + "ms, ";
		smsg += "run for " + CalcUnits.getTimeValue(runctrl.getRunTime()) + "ms.";
		smsg += " saving in " + outFolder.getAbsolutePath();
		E.log(smsg);

		CalcSummary ret = PSICSCalc.writePPP(ctree, channelData, synapseData, sypopData, 
				disc, actconf, aconfig, runctrl, fppp, runJava, flog);
		ret.setFilePattern(fnm);
		rw.close();
		return ret;
	}


	public ArrayList<ViewConfig> getViewConfigs() {
		if (psicsRun == null) {
			readModel(false);
		}
		return psicsRun.getViewConfigs();
	}




}
