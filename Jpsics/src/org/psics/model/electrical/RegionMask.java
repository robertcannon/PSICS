package org.psics.model.electrical;

import org.psics.distrib.PopulationConstraint;
import org.psics.quantity.annotation.Expression;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.StringEnum;
import org.psics.quantity.units.Units;


@ModelType(standalone=false, usedWithin={DistributionRule.class},
		tag="A mask for selecting a region of the cell", info = "")
public class RegionMask {


	@StringEnum(required = true, tag = "Whether regions matching the mask should be included or excluded",
			values = "restrict_to, include, exclude")
	public String action;

	@Expression(required = true, units=Units.truefalse, tag = "Boolean expression selecting a region of the cell. " +
			"The available quantities are the same as for disribution rules.")
	public String where;



	public String getAction() {
		return action;
	}

	public String getCondition() {
		String ret = where;
		if (ret == null) {
			ret = "";
		} else {
			ret = ret.replaceAll("\\.lt\\.", "<");
			ret = ret.replaceAll("\\.gt\\.", ">");
			ret = ret.replaceAll("\\.le\\.", "<=");
			ret = ret.replaceAll("\\.ge\\.", ">=");
			ret = ret.replaceAll("&lt;", "<");
			ret = ret.replaceAll("&gt;", ">");
			
		}
		return ret;
	}

	public void populateFrom(PopulationConstraint dc) {
		 if (dc.isRestrict()) {
			 action="restrict to";

		 } else if (dc.isInclude()) {
			 action="include";

		 } else if (dc.isExclude()) {
			 action = "exclude";

		 }


		 where = dc.getCondition();

	}

	public void setIncludeAction() {
		action = "include";

	}

	public void setWhereMatch(String sm) {
		where = "region=" + sm;
	}


}
