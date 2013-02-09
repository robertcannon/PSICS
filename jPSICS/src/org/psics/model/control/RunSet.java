package org.psics.model.control;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantities;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.phys.QuantityArray;
import org.psics.quantity.units.Units;

@ModelType(info = "Specification of a set of runs  varying either one parameter on its own, or two parameters " +
	"together. If one parameter is changed, only the 'vary', 'values', and 'filepattern' attributes are needed. " +
	"If a second parameter is changed, its name should be set with the 'covary' attrubute, and values array as " +
	"'covalues'.",
		standalone = false,
		tag = "multiple run configuration", usedWithin = { PSICSRun.class })
public class RunSet implements AddableTo {


	@Label(tag = "Name of the parameter to vary. If this is in a different component (different file)," +
			" it should be prefixed with the id of the id of the component", info = "")
	public String vary;


	@Quantities(required = true, tag = "The values to be taken by the variable parameter on separate runs",
			info="They should be expressed as, fpr example, '[1.5, 2, 3.5]ms' where the same units apply to all values. " +
					"The units should match the dimensions of the quantity to be varied.", units=Units.matching, range="(0.001, 5000)")
	public QuantityArray values;



	@Label(tag = "Optional parameter to vary in sync with the parameter specified by 'vary'", info = "")
	public String covary;


	@Quantities(required = true, tag = "Co-varied values, if any",
			info="If set, this should have the same length as the values array", units=Units.matching, range="(0.001, 5000)")
	public QuantityArray covalues;



	@Label(tag = "Template for output file names.",
		info="one set of files is written for each value of the varied quantity.")
	public String filepattern;

	
	@Flag(required = false, tag = "Merge the output files into a single file.")
	public boolean merge = false;
	

	@Container(contentTypes = { CommandSet.class }, tag = "command families")
	public ArrayList<CommandSet> commandSets = new ArrayList<CommandSet>();


	public boolean mergeScan() {
		return merge;
	}

	
	public ArrayList<String> getFileRoots() {
		ArrayList<String> ret = new ArrayList<String>();
		for (int i = 0; i < values.size(); i++) {
			PhysicalQuantity pq = values.get(i);
			ret.add(filepattern.replace("$", pq.getOriginalText()));
		}
		return ret;
	}
	
	
	public String getMergedRoot() {
		return filepattern.replace("$", "merged");
	}
	
	
	private ArrayList<CommandConfig> getCommandConfigs() {
		ArrayList<CommandConfig> ret = new ArrayList<CommandConfig>();
		for (CommandSet cs : commandSets) {
			ret.addAll(cs.getCommandConfigs());
		}
		return ret;
	}



	public ArrayList<RunConfig> getRunConfigs() {

		ArrayList<CommandConfig> acc = getCommandConfigs();

		ArrayList<RunConfig> arc = new ArrayList<RunConfig>();

		for (int i = 0; i < values.size(); i++) {
			PhysicalQuantity pq = values.get(i);
			PhysicalQuantity copq = null;
			if (covalues != null) {
				copq = covalues.get(i);
			}
			RunConfig rc = new RunConfig(vary, pq, covary, copq, filepattern);
			rc.setCommandConfigs(acc);
			arc.add(rc);
		}

		return arc;
	}



	public String getFilePattern() {
		return filepattern;
	}



	public void add(Object obj) {
		if (obj instanceof CommandSet) {
			commandSets.add((CommandSet)obj);
		} else {
			E.error("cant add " + obj);
		}
	}



}
