package org.psics.model.stimrec;

import java.util.ArrayList;

import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Conductance;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.units.Units;

@ModelType(info = "Conductance control profile for use with a conductance clamp",
		standalone = false, tag = "", usedWithin = { Access.class })
public class ConductanceProfile extends Profile<Conductance> {

	@Quantity(range = "[0, 50)", required = false, tag = "Initial conductance", units = Units.nS)
	public Conductance start;

	@Container(contentTypes = { ConductancePulse.class, ConductanceStep.class, ConductanceNoise.class }, tag = "components of the profile")
	public ArrayList<ProfileFeature> features = new ArrayList<ProfileFeature>();



	public void add(Object obj) {
		if (obj instanceof GProfileFeature) {
			features.add((GProfileFeature)obj);
		} else {
			E.error("cant add " + obj);
		}
	}

	public void setStart(Conductance hold) {
			start = hold;
	}

	public ArrayList<ProfileFeature> getFeatures() {
		 return features;
	}

	public double getNonDimStart() {
		return CalcUnits.getConductanceValue(start);
	}


	public PhysicalQuantity getBaseCalcValue() {
		 return new Conductance(0., Units.pS);
	}

}
