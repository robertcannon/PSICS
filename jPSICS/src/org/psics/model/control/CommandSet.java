package org.psics.model.control;

import java.util.ArrayList;

import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantities;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.phys.QuantityArray;
import org.psics.quantity.units.Units;

@ModelType(info = "Specification of a set of runs using modifications of the same command profile", standalone = false,
		tag = "family of command profiles differing in one parameter", usedWithin = { RunSet.class })
public class CommandSet {


	@Label(tag = "Name of the parameter to vary expressed as in the form id:paramname where " +
			" the id is the identifier of the particular commmand element and the paramname is the " +
			"name if the parameter within that component", info = "")
	public String vary;


	@Quantities(required = true, tag = "The values to be taken by the variable parameter on separate runs",
			info="They should be expressed as, for example,  '[1.5, 2, 3.5]ms' where the same units apply to all values " +
					"and should match the dimensions of the varied quantity", units=Units.matching, range="()")
	public QuantityArray values;




	public ArrayList<CommandConfig> getCommandConfigs() {
		ArrayList<CommandConfig> arc = new ArrayList<CommandConfig>();
		for (PhysicalQuantity pq : values.getElements()) {
			CommandConfig rc = new CommandConfig(vary, pq);
			arc.add(rc);
		}
		return arc;
	}




}
