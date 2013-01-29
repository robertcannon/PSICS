package org.psics.model.stimrec;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

@ModelType(info = "Voltage control profile for use with a voltage clamp",
		standalone = false, tag = "", usedWithin = { Access.class, VoltageClamp.class })
public class VoltageProfile extends Profile<Voltage> {

	@Quantity(range = "(-100, 50)", required = false, tag = "Initial holding potential", units = Units.mV)
	public Voltage start;

	@Container(contentTypes = { VoltagePulse.class, VoltageStep.class, VoltageNoise.class }, tag = "components of the profile")
	public ArrayList<ProfileFeature> features = new ArrayList<ProfileFeature>();



	public void add(Object obj) {
		if (obj instanceof VProfileFeature) {
			features.add((VProfileFeature)obj);
		} else {
			E.error("cant add " + obj);
		}
	}

	public void setStart(Voltage hold) {
			start = hold;
	}

	public ArrayList<ProfileFeature> getFeatures() {
		 return features;
	}

	public double getNonDimStart() {
		 return CalcUnits.getVoltageValue(start);
	}


	public PhysicalQuantity getBaseCalcValue() {
		return new Voltage(0., Units.mV);
	}

}
