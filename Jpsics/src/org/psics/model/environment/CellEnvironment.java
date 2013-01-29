package org.psics.model.environment;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.Standalone;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

@ModelType(info = "The cell environment includes the temprature and a list of ions. For each ion the " +
		"reveral potential should be set explicitly rather than providing internal and external solutions.",
		standalone = true, tag = "The envorinment in which the cell model operates", usedWithin = { })
public class CellEnvironment implements AddableTo, Standalone {


	@Identifier(tag = "")
	public String id;

	@Container(contentTypes = { Ion.class }, tag = "Ions involved in the simulation")
	public ArrayList<Ion> ions = new ArrayList<Ion>();


	@Quantity(range = "0, 40", required = false, tag = "temperature in Celsius", units = Units.Celsius)
	public Temperature temperature;


	public HashMap<String, Voltage> getPotentials() {
		HashMap<String, Voltage> revHM = new HashMap<String, Voltage>();
		for (Ion ion : ions) {
			revHM.put(ion.id, ion.reversalPotential);
		}
		return revHM;
	}


	public void add(Object obj) {
		if (obj instanceof Ion) {
			ions.add((Ion)obj);
		} else {
			E.error("cant add " + obj);
		}
	}


	public String getID() {
		return id;
	}


	public Voltage getDefaultReversal(String pid) {
		 Voltage ret = null;
		 for (Ion ion : ions) {
			 if (ion.getID().equals(pid)) {
				 ret = ion.getDefaultReversal();
				 break;
			 }
		 }
		 if (ret == null) {
			 E.error("cant find ion " + pid);
		 }
		 return ret;
	}


	public Temperature getTemperature() {
		Temperature ret = temperature;
		if (ret == null) {
			ret = new Temperature(294.15, Units.K);
		}
		return ret;
	}

}
