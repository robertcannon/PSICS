package org.psics.model.electrical;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;


@ModelType(standalone=false, usedWithin={CellProperties.class},
		tag="A region on a cell to which a set of populations are applied.", info = "This is a conveneince object to " +
				"reduce duplication in channel definitions. If you wish to apply several different populations of " +
				"channels to exactly the same region, instead of creating a distribution rule and " +
				"then referring to it from each population, you can put them all inside a CellRegion with its " +
				"match attribute set. In effect, this just adds the match condition as the first RegionMask for " +
				"each of the enclosed populations.")
public class CellRegion implements AddableTo {

	@Label(info = "", tag = "Region to which these properties apply")
	public String match;

	@Container(contentTypes = {ChannelPopulation.class}, tag = "Channel populations")
	public ArrayList<ChannelPopulation> populations = new ArrayList<ChannelPopulation>();


//	 TODO make generic?
	public void add(Object obj) {
		if (obj instanceof ChannelPopulation) {
			populations.add((ChannelPopulation)obj);
		} else {
			E.error("cant add " + obj);
		}
	}


	public String getMatch() {
		return match;
	}


}
