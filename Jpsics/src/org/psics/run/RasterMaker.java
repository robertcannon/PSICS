package org.psics.run;

import java.awt.Color;
import java.io.File;

import org.catacomb.dataview.Plot;
import org.catacomb.numeric.data.AsciiIO;
import org.catacomb.numeric.data.DataTable;
import org.catacomb.util.FileUtil;
import org.psics.be.E;
import org.psics.model.display.Raster;


public class RasterMaker {

	Raster raster;

	public RasterMaker(Raster ras) {
		raster = ras;
	}



	public void makeImage(File dir) {

		Plot p = new Plot();
		p.setBackgroundColor(Color.black);


		File f = new File(dir, raster.getFileName());

		// bit of a hack
		if (!f.exists()) {
			File ftry = new File(f.getParentFile().getParentFile(), f.getName());
			if (ftry.exists()) {
				FileUtil.copyFile(ftry, f);
			}
		}

		DataTable tbl = null;
		String s = FileUtil.readFirstLine(f);
		if (s.indexOf("FPSICS2") >= 0) {
			tbl = PSICSDataReader.readTable(f);
		} else {
			tbl = AsciiIO.readTable(f);
		}

		String[] headings = tbl.getHeadings();
		String c1 = headings[1];
		int nrec = 0;
		int nrun = 0;
 		for (int i = 0; i < headings.length; i++) {
			if (c1.equals(headings[i])) {
				nrun += 1;
			}
		}
 		nrec = (headings.length - 1) / nrun;

 		E.info("nrec, nryn " + nrec + " " + nrun);


		double[] times = tbl.getColumn(0);
		double tmin = times[0];
		double tmax = times[times.length - 1];
		double thresh = raster.getThreshold();

		int nmax = nrec * (nrun + 4);
		for (int i = 0; i < nrec; i++) {
			for (int j = 0; j < nrun; j++) {
				double y = nmax - i * (nrun + 4) - j;
				double[] vs = tbl.getColumn(1 + j * nrec + i);
				p.addRasterRow(times, vs, raster.getVMin(), raster.getVMax(), y, thresh);
			}
		}


 		double[] xyxy = {tmin, 0., tmax, nmax};


 		File fim = new File(dir, raster.getID() + ".png");
		p.makeImage(raster.getWidth(), raster.getHeight(), xyxy, fim);
		E.info("made file " + fim);
	}






}
