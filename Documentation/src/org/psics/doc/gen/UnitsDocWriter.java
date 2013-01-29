package org.psics.doc.gen;

import java.io.File;

import org.psics.be.E;
import org.psics.be.Element;
import org.psics.om.OmBuilder;
import org.psics.quantity.units.DimensionSet;
import org.psics.quantity.units.Units;
import org.psics.util.FileUtil;


/*private int[] mltak = new int[5];

private int pTEN;

private boolean pure;
private double fac;

private String name;
private String check;
*/

public class UnitsDocWriter {



	static String TYPE_SPEC_TRANSFORM_PATH = "TypeSpec.xsl";




	public UnitsDocWriter() {

	}



	public static void main(String[] argv) {
		File destdir = null;
		if (argv.length > 0) {
			destdir = new File(argv[0]);
		} else {
			destdir = new File("../tmp");
			E.info("writing xml to " + destdir.getAbsolutePath());
		}


		File fout = new File(destdir, "UnitsList.xml");

		OmBuilder omb = new OmBuilder();
		Element elt = omb.newElement("UnitsList");

		for (Units u : Units.values()) {
			Element felt = omb.newElement("Units");

			DimensionSet dset = u.getDimensionSet();
			omb.addAttribute(felt, "symbol", u.name());
			omb.addAttribute(felt, "name", dset.getName());
			omb.addAttribute(felt, "pure", dset.isPure() ? "true" : "false");
			
			double fac = dset.getFac();
			
			String sfac = "";
			if (Math.abs(fac) < 1.e-9) {
			//	fac = 1.0; // maybe fac should be 1 for the ones where it is not used
				sfac = "";
			} else {
				sfac = String.format("%13.5g", fac);
				
			}
				omb.addAttribute(felt, "fac", sfac);
			
			omb.addAttribute(felt, "pten", dset.getPTen());

			omb.addAttribute(felt, "M", dset.getMLTAK()[0]);
			omb.addAttribute(felt, "L", dset.getMLTAK()[1]);
			omb.addAttribute(felt, "T", dset.getMLTAK()[2]);
			omb.addAttribute(felt, "A", dset.getMLTAK()[3]);
			omb.addAttribute(felt, "K", dset.getMLTAK()[4]);

			omb.addElement(elt, felt);
		}



		String selt = elt.serialize();
		FileUtil.writeStringToFile(selt, fout);

		// similar blocks for morph, run control etc TODO
	}








}
