package org.psics.model.control;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.model.ModelMap;
import org.psics.num.CalcSummary;
import org.psics.quantity.DimensionalExpression;
import org.psics.quantity.DimensionalQuantity;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.IntegerQuantity;
import org.psics.quantity.phys.PhysicalExpression;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.units.Units;




@ModelType(info = "", standalone = false, tag = "", usedWithin = {PSICSRun.class})
public class RunConfig {

	@Label(info = "", tag = "")
	public String vary;

	@Quantity(range = "", required = false, tag = "", units = Units.none)
	public PhysicalQuantity value;


	@Label(info = "", tag = "")
	public String covary;

	@Quantity(range = "", required = false, tag = "", units = Units.none)
	public PhysicalQuantity covalue;

	@Label(info = "", tag = "")
	public String filepattern;




	ArrayList<CommandConfig> commandConfigs;


	public RunConfig() {

	}


	public RunConfig(String var, PhysicalQuantity v, String covar, PhysicalQuantity cov, String fpat) {
		vary = var;
		value = v;
		covary = covar;
		covalue = cov;
		filepattern = fpat;

		if (value == null) {
			E.error("made a run config with a null value?");
		} else if (vary == null) {
			E.error("made a run config with a null vary quantity?");
		}


	}


	public String getValueString() {
		return value.getOriginalText();
	}


	// TODO - more subtle way to access models!
	public void applyTo(Object obj) {
		ModelMap mm = ModelMap.buildMap(obj);

		setQuantity(mm, vary, value);
		if (covary != null && covary.length() > 0) {
			setQuantity(mm, covary, covalue);
		}

	}



	private void setQuantity(ModelMap mm, String var, PhysicalQuantity pq) {
		if (mm.hasQuantityItem(var)) {
			DimensionalQuantity dq = mm.getQuantity(var);
			if (dq instanceof PhysicalQuantity) {
				((PhysicalQuantity)dq).setValue(pq);
			} else {
				((IntegerQuantity)dq).setIntValue((int)pq.getNativeValue(), Units.none);
				// POSERR - dimensional int quantities?
			}
			
		} else if (mm.hasExpressionItem(var)) {
				DimensionalExpression de = mm.getExpression(var);
				if (de instanceof PhysicalExpression) {
					((PhysicalExpression)de).setValue(pq);
				} else {
					E.missing();
				}
			
		} else {
			E.oneLineError("cant set quantity " + var + " - not found");
			if (var.indexOf(":") > 0) {
				mm.printAvailableObjects();
			} else {
				mm.printAvailableSimple();
			}
		}
	}



	public int nCommands() {
		int ret = 0;
		if (commandConfigs != null) {
			ret = commandConfigs.size();
		}
		return ret;
	}


	public void setCommandConfigs(ArrayList<CommandConfig> acc) {
		commandConfigs = acc;
	}

	public ArrayList<CommandConfig> getCommandConfigs() {
		return commandConfigs;
	}


	public void summarize(CalcSummary cs) {
		cs.setVariableName(vary);
		cs.setVariableText(value.getOriginalText());
		cs.setVariableValue(value.getNativeValue());
	}


}
