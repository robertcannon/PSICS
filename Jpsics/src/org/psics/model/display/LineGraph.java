package org.psics.model.display;

import java.util.ArrayList;

import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.ModelType;

@ModelType(info = "This is the standard plot type for PSICS output.  It should contain " +
		"specifications of one or more data sources, one or more views and optional axis " +
		"definitions.", standalone = false, tag = "A plot of recorder  values against time", usedWithin = { ViewConfig.class })
public class LineGraph extends BaseGraph {

	@Container(contentTypes = { LineSet.class, Line.class}, tag = "Data sources")
	public ArrayList<BaseLineSet> c_lineSets = new ArrayList<BaseLineSet>();



	public void add(Object obj) {
		if (obj instanceof BaseLineSet) {
			c_lineSets.add((BaseLineSet)obj);
			// this case catches plain lines too, since they extend LineSet

		} else {
			super.addItem(obj);
		}


	}


	public ArrayList<BaseLineSet> getBaseLineSets() {
		return c_lineSets;
	}


}
