package org.psics.model.control;

import java.lang.reflect.Field;

import org.psics.be.E;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.units.Units;




@ModelType(info = "", standalone = false, tag = "", usedWithin = {})
public class CommandConfig {

	@Label(info = "", tag = "")
	public String vary;

	@Quantity(range = "", required = true, tag = "?", units = Units.any)
	public PhysicalQuantity value;

	String tgtid;
	String fieldname;


	public CommandConfig() {

	}

	public CommandConfig(String var, PhysicalQuantity v) {
		vary = var;
		value = v;

		tgtid = null;
		fieldname = vary;
		int ic = var.indexOf(":");
		if (ic > 0) {
			tgtid = vary.substring(0, ic);
			fieldname = var.substring(ic + 1, var.length());
		}
	}


	public String getValueString() {
		return value.getOriginalText();
	}


	// TODO - more subtle way to access models!
	public void applyTo(Object obj) {

		boolean done = false;

		for (Field f : obj.getClass().getFields()) {
			if (f.getName().equals(fieldname)) {
				try {
				PhysicalQuantity pq = (PhysicalQuantity)(f.get(obj));
				pq.setValue(value);
				done = true;

				} catch (Exception ex) {
					E.error("? " + ex);
				}

				break;
			}
		}
		if (!done) {
			E.error("could't set command config target: no field " + fieldname + " on " + obj);
		}

	}



	public String getTargetID() {
		return tgtid;
	}



}
