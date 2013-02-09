package org.psics.run;

import java.io.File;
import java.util.ArrayList;

import org.catacomb.dataview.Plot;
import org.psics.be.E;
import org.catacomb.util.FileUtil;
import org.psics.model.display.BaseGraph;
import org.psics.model.display.LineGraph;
import org.psics.model.display.View;


public class ViewMaker {

	BaseGraph graph;

	public ViewMaker(BaseGraph bg) {
		graph = bg;
	}



	public void makeImages(File dir) {

		ArrayList<String> ret = null;

		Plot p = new Plot();
		p.setXLabel(graph.getXAxis().getLabel());
		p.setYLabel(graph.getYAxis().getLabel());


		if (graph instanceof LineGraph) {
			ret = LineGraphPlotter.populatePlot((LineGraph)graph, dir, p);

		} else {
			E.missing("cant make images for " + graph);
		}


		String lastid = "";
		for (View view : graph.getViews()) {
			p.makeImage(graph.getWidth(), graph.getHeight(), view.getXYXYLimits(),
						new File(dir, view.getID() + ".png"));
			lastid = view.getID();
		}

		if (ret != null && ret.size() > 0) {
			StringBuffer sb = new StringBuffer();
			for (String s : ret) {
				sb.append(s);
				sb.append("<br/>\n");
			}
			FileUtil.writeStringToFile(sb.toString(), new File(dir, lastid + ".txt"));
		}
	}






}
