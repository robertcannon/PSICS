package org.psics.model.control;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.Standalone;
import org.psics.be.Textalizer;
import org.psics.model.activity.Activity;
import org.psics.model.channel.KSChannel;
import org.psics.model.display.ViewConfig;
import org.psics.model.electrical.CellProperties;
import org.psics.model.environment.CellEnvironment;
import org.psics.model.morph.CellMorphology;
import org.psics.model.morph.MorphologySource;
import org.psics.model.stimrec.Access;
import org.psics.num.AccessConfig;
import org.psics.num.ActivityConfig;
import org.psics.num.CompartmentTree;
import org.psics.num.Discretization;
import org.psics.num.RunControl;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.annotation.FolderPath;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.Metadata;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.StringEnum;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;



@ModelType(standalone=true, usedWithin={},
		tag="Overall specification and control of a calcluation", info="This is the first " +
		"object read when the calculation starts. It says where to find model components, " +
		"what simulation to perform and what to do with the results.")
public class PSICSRun implements AddableTo, Standalone {


	@Identifier(tag = "")
	public String id;

	@ReferenceByIdentifier(tag="The identifier of the cell morphology to be used",
			targetTypes={CellMorphology.class}, required=true, location=Location.global)
	public String morphology;
	public CellMorphology r_morphology;


	@ReferenceByIdentifier(tag="The identifier of the channel distribution to be used",
			targetTypes={CellProperties.class}, required=true, location=Location.global)
	public String properties;
	public CellProperties r_properties;


	@ReferenceByIdentifier(tag="The identifier of the cell environment to be used",
			targetTypes={CellEnvironment.class}, required=true, location=Location.global)
	public String environment;
	public CellEnvironment r_environment;


	@ReferenceByIdentifier(tag="The identifier of the access configuration to be used",
			targetTypes={Access.class}, required=false, location=Location.global)
	public String access;
	public Access r_access;


	@ReferenceByIdentifier(tag="The identifier of the activity (incoming spikes etc) configuration to be " +
			"used, if any",
			targetTypes={Activity.class}, required=false, location=Location.global)
	public String activity;
	public Activity r_activity;
	
	
	@Metadata(tag = "Brief description of the model", info="")
	public String info;


	@Quantity(range = "(0.001, 1)", required=false, tag = "Timestep for fixed step calculations", units = Units.ms)
	public Time timeStep = new Time(0.1, Units.ms);

	@Quantity(range = "(1, 1000)", required=false, tag = "Time to run the model for", units = Units.ms)
	public Time runTime = new Time(100, Units.ms);


	@FolderPath(fallback = "", required = false, tag = "Folder to use for output files instead of " +
			"the default location based on the name of main model file.")
	public String outputFolder;


	@StringEnum(required = false, tag = "Numerical method to be used (defaults to weighted Crank-Nicolson)",
			values = "CRANK_NICOLSON, IMPLICIT_EULER, FORWARD_EULER, WCN_0.51, WCN_0.6")
	public String method;

	@Quantity(range = "[0., 1.]", required=false, units = Units.none, tag="Explict control of temporal differencing as " +
			"an alternative to selecting one of the fixed methods")
	public NDValue tdWeighting = new NDValue(-1.);


	@IntegerNumber(range = "(10, 100000)", required=false, tag="Maximum number of compartments allowed. If the " +
			"discretization generates more points, the simulation will be abandoned.")
	public NDNumber maxPoints = new NDNumber(0);


	@IntegerNumber(range="(1,100)", required=false, tag="Number of repeates of the same recording")
	public NDNumber repeats = new NDNumber(1);


	@Quantity(range = "(-80, -50)", required=false, tag="Potential to initialize the cell to at the " +
			"start of the calculation",
			units = Units.mV)
	public Voltage startPotential = new Voltage(-70., Units.mV);


	@IntegerNumber(range = "(10, 1000)", required=false, tag="The threshold beyond which channel calculations use the " +
			"ensemble limit. This can be overridden for particular channels with ChannelStochThreshold blocks.")
	public NDNumber stochThreshold = new NDNumber(0);


	@Container(contentTypes = {ChannelStochThreshold.class}, tag = "Per-channel stochasticity thresholds. " +
			"Allows some channel types to be treated stochastically against a continuous background " +
			"from the rest.")
	public ArrayList<ChannelStochThreshold>  perChannelThresholds = new ArrayList<ChannelStochThreshold>();


	@Container(contentTypes = {ModelFolder.class}, tag = "Paths to search for models")
	public ArrayList<ModelFolder> modelFolders = new ArrayList<ModelFolder>();


	@Container(contentTypes = {StructureDiscretization.class}, tag = "Structure discretization parameters")
	public ArrayList<StructureDiscretization> structureDiscretizations = new ArrayList<StructureDiscretization>();


	@Container(contentTypes = {ChannelDiscretization.class}, tag = "Channel discretization parameters")
	public ArrayList<ChannelDiscretization> discretizations = new ArrayList<ChannelDiscretization>();

	@Container(contentTypes = { MorphologySource.class }, tag = "Imports of external morphologies")
	public ArrayList<MorphologySource> morphologySources = new ArrayList<MorphologySource>();


	@Container(contentTypes = {RunSet.class}, tag = "Optional mult-run specifications")
	public ArrayList<RunSet> runSets = new ArrayList<RunSet>();

	@Container(contentTypes = {ViewConfig.class}, tag = "Default views of the results")
	public ArrayList<ViewConfig> viewConfigs = new ArrayList<ViewConfig>();

	@Container(contentTypes = {About.class}, tag = "Extended textual information about the model")
	public ArrayList<About> abouts = new ArrayList<About>();

	@Flag(required = false, tag = "Use quicker, compartment based method for allocating " +
			"channels instead of giving them all exact 3D locations")
	public boolean quickChannels = false;


	@Flag(required = false, tag = "Advance every channel individually. This is much slower " +
	"in general and is intended to provide comparisons with results using the normal " +
	"approximations. Please let us know if you find a case that shows statistically " +
	"significant differences.")
	public boolean oneByOne = false;

	@Flag(required = false, tag = "If set, model segments  with flat (rather than " +
			"hemispherical) ends and treat minor points as originating from the " +
			"center of the parent point, not the surface. This is mainly useful for " +
			"exact comparisons with results from models that make this assumption.")
	public boolean squareCaps = false;





	// TODO genericize adding to containers
	public void add(Object obj) {
		if (obj instanceof ModelFolder) {
			modelFolders.add((ModelFolder)obj);

		} else if (obj instanceof ChannelDiscretization) {
			discretizations.add((ChannelDiscretization)obj);

		} else if (obj instanceof StructureDiscretization) {
			structureDiscretizations.add((StructureDiscretization)obj);

		} else if (obj instanceof MorphologySource) {
			morphologySources.add((MorphologySource)obj);

		} else if (obj instanceof ChannelStochThreshold) {
			perChannelThresholds.add((ChannelStochThreshold)obj);

		} else if (obj instanceof RunSet) {
			runSets.add((RunSet)obj);

		} else if (obj instanceof ViewConfig) {
			viewConfigs.add((ViewConfig)obj);

		} else if (obj instanceof About) {
			abouts.add((About)obj);

		} else {
			E.error("cant add " + obj);
		}
	}


	public String getID() {
		return id;
	}


	public void resolve() {
		if (r_morphology == null) {
			if (morphologySources.size() == 0) {
				E.error("No morphology file specified.\nN.B. there are two ways to set the morphology:\n" +
						" - the attribute morphology=\"cell_id\" can be set for PSICS XML morphologies\n" +
						" - imported SWC or Neuron files need a separate element of the form " +
						"<MorphologySource format=\"swc\" file=\"abc.swc\"/>\n");
			} else {
				r_morphology = morphologySources.get(0).getCellMorphology();
			}
		} else {
			r_morphology.resolve();
		}

		if (r_properties == null) {
			E.error("no channel cell properties file specified");
		} else {
			r_properties.resolve();
		}


		if (r_environment == null) {
			E.error("no environment file specified");
		} else {
		//	r_environment.resolve();
		}
	}


	public CellMorphology getCellMorphology() {
		return r_morphology;
	}


	public StructureDiscretization getStructureDiscretization() {
		StructureDiscretization ret = null;
		if (structureDiscretizations.size() > 0) {
			ret = structureDiscretizations.get(0);
		} else {
			ret = new StructureDiscretization();
			ret.defaultInit();
		}
		return ret;
	}


	public int getMaxPoints() {
		int imp = maxPoints.getNativeValue();
		if (imp <= 0) {
			imp = 10000;
		}
		return imp;
	}


	public CellProperties getCellProperties() {
		return r_properties;
	}



	public RunControl getRunControl() {
		 RunControl ret = new RunControl(runTime, timeStep, startPotential);

		 if (oneByOne) {
			 ret.setOneByOne();
		 }
		 ret.setDefaultStochThreshold(stochThreshold.getNativeValue());
		 for (ChannelStochThreshold cst : perChannelThresholds) {
			 ret.setStochThreshold(cst.getChannelID(), cst.getThreshold());
		 }

		 // POSERR these hardcoded values shouldn't be here: could use an enum sibling to RunControl.
		 double wf = -1;
		 if (method != null && method.trim().length() > 0) {
			 String s = method.trim().toLowerCase();
			 if (s.equals("forward_euler")) {
				 wf = 0.;
			 } else if (s.equals("implicit_euler")) {
				 wf = 1.;
			 } else if (s.equals("crank_nicolson")) {
				 wf = 0.5;
			 } else if (s.equals("wcn_0.51") || s.equals("weighted_crank_nicolson")) {
				 wf = 0.51;
			 } else if (s.equals("wcn_0.6")) {
				 wf = 0.6;
			 } else {
				 E.warning("unrecognized value for the method - using default: " + method);
			 }
		 }

		 if (tdWeighting != null && tdWeighting.getValue() >= 0) {
			 double w = tdWeighting.getNativeValue();
			 if (Math.abs(w - wf) > 0.01) {
				 if (wf >= 0) {
					 E.warning("method specifiation being overridden by explicit weight factor");
				 }
				 ret.setWeightingFactor(w);
			 }
		 } else if (wf >= 0) {
			 ret.setWeightingFactor(wf);
		 } else {
			 // don't set it: let the numerics use the default;
		 }

		 ret.setUseNative(true);   // TODO may want a way to force the java version

		 return ret;
	}




	public Discretization getDiscretization() {
		Discretization ret = null;
		if (discretizations.size() > 0) {
			ret = (discretizations.get(0).getDiscretization());
		} else {
			ret = new Discretization();
		}
		return ret;
	}


	public CellEnvironment getCellEnvironment() {
		return r_environment;
	}


	public AccessConfig getAccessConfig(ArrayList<CommandConfig> commands, CompartmentTree ctree) {
		AccessConfig ret = null;
		if (r_access != null) {
			ret = r_access.getAccessConfig(commands, ctree);
		} else {
			ret = new AccessConfig();
		}
		return ret;
	}


	public ActivityConfig getActivityConfig(HashMap<String, Integer> popIDs) {
		ActivityConfig ret = null;
		if (r_activity != null) {
			ret = r_activity.getActivityConfig(popIDs);
		} else {
			ret = new ActivityConfig();
		}
		return ret;
	}
	
	

	public boolean isMultiRun() {
		boolean ret = false;
		if (runSets.size() > 0) {
			ret = true;
		}
		return ret;
	}


	public ArrayList<RunSet> getRunSets() {
		return runSets;
	}



	public ArrayList<ViewConfig> getViewConfigs() {
		 return viewConfigs;
	}



	public ArrayList<String> getOutputImageNames() {
		ArrayList<String> ret = new ArrayList<String>();
		for (ViewConfig vc : viewConfigs) {
			ret.addAll(vc.getImageNames());
		}
		return ret;
	}



	public String getInfo() {
		String ret = "";
		if (info != null) {
			ret = info;
		}
		return ret;
	}





	public int getNRepeat() {
		 return repeats.getValue();
	}




	public ArrayList<String> getInfoParas() {

		ArrayList<String> ret = new ArrayList<String>();
		if (abouts != null) {
			for (About ab : abouts) {
				ret.add(ab.getText());
			}
		}
		return ret;
	}


	public boolean populateByCompartments() {
		return quickChannels;
	}


	public boolean getSquareCaps() {
		return squareCaps;
	}


	public String getMorphID() {
		String ret = morphology;
		if (r_morphology != null) {
			ret = r_morphology.getID();
		}
		return ret;
	}


	public String getTextVersion(Textalizer tlz) {
		String ret = null;
		if (r_properties != null) {
			ArrayList<KSChannel> aks = r_properties.getKSChannels();

			for (KSChannel ksc : aks) {
				tlz.add(ksc);
			}

			ret = tlz.makeText();
		}
		return ret;
	}


}
