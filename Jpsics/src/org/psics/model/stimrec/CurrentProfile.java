package org.psics.model.stimrec;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Current;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.units.Units;

@ModelType(info = "Current control profile for use with a current clamp",
		standalone = false, tag = "", usedWithin = { Access.class, CurrentClamp.class })
public class CurrentProfile extends Profile<Current> {

	@Quantity(range = "(-50, 50)", required = false, tag = "Initial holding current", units = Units.nA)
	public Current start;

	@Container(contentTypes = { CurrentPulse.class, CurrentStep.class, CurrentNoise.class }, tag = "components of the profile")
	public ArrayList<ProfileFeature> features = new ArrayList<ProfileFeature>();

	public void add(Object obj) {
		if (obj instanceof CProfileFeature) {
			features.add((CProfileFeature)obj);
		} else {
			E.error("cant add " + obj);
		}
	}

	public void setStart(Current hold) {
		 start = hold;
	}

	public double getNonDimStart() {
		return CalcUnits.getCurrentValue(start);
	}

	public ArrayList<ProfileFeature> getFeatures() {
		return features;
	}


	public PhysicalQuantity getBaseCalcValue() {
		return new Current(0., Units.nA);
	}


}
