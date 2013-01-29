package org.psics.model.stimrec;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.Standalone;
import org.psics.model.control.CommandConfig;
import org.psics.num.AccessConfig;
import org.psics.num.Accessor;
import org.psics.num.CalcUnits;
import org.psics.num.CompartmentTree;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.Logical;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;



@ModelType(info = "Voltage clamps, current clamps, conductance clamps and voltage recorders for stimulating and" +
		"recording from the model.",
		standalone = true,
		tag = "Stimulation and recording", usedWithin = { })
public class Access implements AddableTo, Standalone {

	@Identifier(tag="Identifier (name) for the stimulation/recording specification")
	public String id;


	@Quantity(range = "(0.001, 5)", required = false, tag = "Interval at which the recorders should save their values", units = Units.ms)
	public Time saveInterval;

	@Flag(required = false, tag = "If set, split the output file into one file per column named " +
			"according to the column headings")
	public boolean separateFiles;

	@Label(tag = "file extension to use for text files", info="The default extension is '.txt'. It can be " +
			"changed by setting an alternative extension here.")
	public String separateFilesExtension;
	
	@Container(contentTypes = {CellLocation.class}, tag = "Geometrically defined positions on the cell")
	public ArrayList<CellLocation> locations = new ArrayList<CellLocation>();


	@Container(contentTypes = {VoltageClamp.class, CurrentClamp.class, ConductanceClamp.class}, tag = "Clamps aplied to the cell")
	public ArrayList<Clamp> clamps = new ArrayList<Clamp>();


	@Container(contentTypes = { VoltageRecorder.class }, tag = "Recorders - these do not affect the cell at all")
	public ArrayList<VoltageRecorder> recorders = new ArrayList<VoltageRecorder>();

	@Container(contentTypes = { SmartRecorder.class }, tag = "SmartRecorders - recording quantities that are not normally accessible")
	public ArrayList<SmartRecorder> smartRecorders = new ArrayList<SmartRecorder>();

	@Flag(required = false, tag = "If set, then the voltage (for current claps) or current (for voltage clamps) "+
			"is recorded for each clamp as though it was doubling up as a recorder. " +
			"For large numbers of clamps, particularly with aoto-generated distributions, this can be set " +
			"to 'false' to reduce the output file sizes.")
	public boolean recordClamps = true;




	public void add(Object obj) {
		if (obj instanceof Clamp) {
			clamps.add((Clamp)obj);

		} else if (obj instanceof VoltageRecorder) {
			recorders.add((VoltageRecorder)obj);

		} else if (obj instanceof SmartRecorder) {
			smartRecorders.add((SmartRecorder)obj);

		} else if (obj instanceof CellLocation) {
			locations.add((CellLocation)obj);

		} else {
			E.error("cant add " + obj);
		}

	}

	public String getID() {
		return id;
	}

	public AccessConfig getAccessConfig(ArrayList<CommandConfig> commands, CompartmentTree ctree) {
		AccessConfig ret = new AccessConfig();
		if (saveInterval != null) {
			ret.setSaveInterval(CalcUnits.getTimeValue(saveInterval));
		}

		if (separateFiles) {
			ret.setSeparateFiles();
			if (separateFilesExtension != null) {
				ret.setSeparateFilesExtension(separateFilesExtension);
			} else {
				ret.setSeparateFilesExtension(".txt");
			}
		}

		if (recordClamps) {
			ret.setRecordClamps();
		}


		for (Clamp clamp : clamps) {

			String tgt = getTargetPointID(clamp, ctree);

			if (clamp instanceof VoltageClamp) {
				VoltageProfile vprof = ((VoltageClamp)clamp).getVoltageProfile();
				ret.addVoltageClamp(clamp.getID(), tgt, vprof.makeCommands(commands), clamp.getStyle(),
						clamp.getRecordable());

			} else if (clamp instanceof CurrentClamp) {
				CurrentProfile cprof = ((CurrentClamp)clamp).getCurrentProfile();
				ret.addCurrentClamp(clamp.getID(), tgt, cprof.makeCommands(commands), clamp.getStyle(),
						clamp.getRecordable());

			} else if (clamp instanceof ConductanceClamp) {
				double vto = ((ConductanceClamp)clamp).getDimlessPotential();
				ConductanceProfile cprof = ((ConductanceClamp)clamp).getConductanceProfile();
				ret.addConductanceClamp(clamp.getID(), tgt, vto, cprof.makeCommands(commands), clamp.getStyle(),
						clamp.getRecordable());


			} else {
				E.error("?");
			}
		}

		for (VoltageRecorder vr : recorders) {
			String tgt = getTargetPointID(vr, ctree);
			ret.addRecorder(vr.getID(), tgt, vr.getStyle());
		}

		for (SmartRecorder sr : smartRecorders) {
			double r = sr.getRange();
			if (r > 0) {
				E.oneLineWarning("smart recorder range not yet supported - using a single compartment");
			}
			int m = sr.getModality();
			/*
			if (r < 0) {
				if (m == Accessor.CURRENT) {
					ret.addGlobalCurrentRecorder(sr.getID(), sr.getStyle(), sr.getChannelType());

				} else if (m == Accessor.CONDUCTANCE) {
					ret.addGlobalConductanceRecorder(sr.getID(), sr.getStyle(), sr.getChannelType());
				}
			} else {
			*/

				String tgt = getTargetPointID(sr, ctree);
				if (m == Accessor.CURRENT) {
					ret.addCurrentRecorder(sr.getID(), tgt, sr.getStyle(), sr.getChannelType());

				} else if (m == Accessor.CONDUCTANCE) {
					ret.addConductanceRecorder(sr.getID(), tgt, sr.getStyle(), sr.getChannelType());
				}


		}

		return ret;
	}



	private String getTargetPointID(DisplayableRecorder dr, CompartmentTree ctree) {
		String tgt = null;
		if (dr.hasLabelTarget()) {
			tgt = dr.getAt();

		} else if (dr.hasLocationTarget()) {
			tgt = dr.getLocation().identifyOn(ctree);

		} else if (dr.hasDistance()) {
			tgt = ctree.getRelativeCompartmentId(CalcUnits.getLengthValue(dr.getDistance()),
					dr.getFrom(), dr.getTowards());

		} else {
			E.error("a clamp must have a 'at' or 'location' specified");
		}

		return tgt;
	}


}
