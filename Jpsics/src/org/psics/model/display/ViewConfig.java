package org.psics.model.display;

import java.util.ArrayList;
import java.util.Collection;

import org.psics.be.AddableTo;
import org.psics.model.control.PSICSRun;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.annotation.ModelType;

@ModelType(info = "Postprocessing and visualization to be run on output data", standalone = false,
		tag = "Default visualization definitions", usedWithin = { PSICSRun.class })
public class ViewConfig implements AddableTo {

	@Container(contentTypes = { LineGraph.class, PowerSpectrum.class, MeanVariance.class},
			tag = "Plot families. Each plot family may contain multiple " +
			"views with different axis ranges")
	public ArrayList<BaseGraph> c_graphs = new ArrayList<BaseGraph>();


	@Container(contentTypes = {Raster.class},
			tag = "Rasters")
	public ArrayList<Raster> c_rasters = new ArrayList<Raster>();

	@Flag(required = false, tag = "Set to false to suppress creating morphology plots")
	public boolean morphology = true;

	@Flag(required = false, tag = "Set to true to export the morphology data so it can be plotted independently")
	public boolean morphologyData = false;
	

	public void add(Object obj) {
		if (obj instanceof BaseGraph) {
			c_graphs.add((BaseGraph)obj);

		} else if (obj instanceof Raster) {
			c_rasters.add((Raster)obj);
		}
	}


	public ArrayList<BaseGraph> getGraphs() {
		return c_graphs;
	}


	public boolean showMorph() {
		return morphology;
	}
	
	public boolean showMorphologyData() {
		return morphologyData;
	}

	public Collection<? extends String> getImageNames() {
		 ArrayList<String> ret = new ArrayList<String>();

		 for (BaseGraph bg : c_graphs) {
			 for (View v : bg.getViews()) {
				 ret.add(v.getID());
			 }
		 }
		 int ir = 0;
		 for (Raster r : c_rasters) {
			 String s = r.getID();
			 if (s == null) {
				  s = "raster" + ir;
				  r.setID(s);
			 }
			 ret.add(s);
			 ir += 1;
		 }
		 return ret;
	}


	public ArrayList<Raster> getRasters() {
		return c_rasters;
	}




}
