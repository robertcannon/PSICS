package org.psics.num;

import java.io.File;
import java.util.ArrayList;

import org.psics.be.E;
import org.psics.num.model.channel.ChannelData;
import org.psics.out.ResultsWriter;
import org.psics.util.FileUtil;


public class JCore {


	public static double run(double xdt, double xruntime, double v0, double wf, int nRun, ChannelData channelData,
			CompartmentMatrix cptmtx, AccessConfig aconfig, File fppp) {

		ResultsWriter rw = new ResultsWriter(FileUtil.getSiblingFile(fppp, ""));

		cptmtx.setPotential(v0);
		if (wf >= 0.) {
			cptmtx.setWeightingFactor(wf);
		}
		int stepType = 0; // other values if tabulate multiple possible step
							// lengths;

		// rw.writeString(cptmtx.getVText(0.));

		int nwi = (int) (Math.round(aconfig.getSaveInterval() / xdt));
		if (nwi < 1) {
			nwi = 1;
		}

		// rw.writeDataNames("recorders", "time", aconfig.getRecorderIDs());
		ArrayList<String> resnames = new ArrayList<String>();

		if (nRun > 1) {
			// E.info("Multiple profiles: " + nRun + " command sequences being applied to the same model");
		}

		long ltime1 = System.currentTimeMillis();

		for (int irun = 0; irun < nRun; irun++) {
			cptmtx.setPotential(v0);
			cptmtx.allocateChannels(channelData);
			cptmtx.instantiateChannels();

			String dsname = "";
			if (nRun > 1) {
				dsname += irun;
			}
			resnames.add(dsname);

			aconfig.setCommand(irun);
			ResultsWriter rwdat = rw.getSibling(dsname, ResultsWriter.TEXT);

			rwdat.writeDataNames("time", aconfig.getRecorderIDs());
		//	E.info("writing table data to " + rwdat);
			int iw = 0;
			double xtime = 0.;
			while (xtime < xruntime) {
				if (iw % nwi == 0) {
					double[] vs = aconfig.getRecorderValues();
					if (irun == 0) {
						//rw.writeString(cptmtx.getVText(xtime));
						//rw.writeData("recorders", xtime, vs);
					}

					rwdat.writeData(xtime, vs);

					iw = 0;
				}
				iw += 1;

				aconfig.advanceControl(xtime, xdt);


				cptmtx.advanceChannels(stepType);

				cptmtx.diffuse(xdt);
				xtime += xdt;
			}
			rwdat.close();
			//rw.writeData(dsname, xtime, aconfig.getRecorderValues());
			//rw.closeData(dsname);

		}

		long ltime2 = System.currentTimeMillis();

		if (nRun > 1) {
			E.missing();
			// rw.mergeSiblings(resnames, "", ResultsWriter.TEXT);

		}

		return 0.001 * (ltime2 - ltime1);
	}

}
