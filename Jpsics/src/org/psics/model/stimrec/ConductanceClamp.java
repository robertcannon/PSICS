package org.psics.model.stimrec;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.phys.Conductance;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

@ModelType(info = "A fixed or variable conductance to a fixed potential, acting " +
		"like an imperfect voltage clamp. The conductance can be changed as a function of time. " +
		"It provides a simpler way to stimulate a small process than a direct current injection" +
		"since the resultant internal potential is far less sensitive to the " +
		"applied conductane than to an applied current. The 'at' attribute should match the id of" +
		" a point on the cell.", standalone = false, tag = "Conductance clamp", usedWithin = { Access.class })
public class ConductanceClamp extends Clamp implements AddableTo {

	@Quantity(range = "", required = false, tag = "", units = Units.mV)
	public Voltage potential;

	@Quantity(range = "", required = false, tag = "", units = Units.nS)
	public Conductance hold;

	// could want this local
	@ReferenceByIdentifier(location = Location.global, required = false, tag = "", targetTypes = { ConductanceProfile.class })
	public String profile;
	public ConductanceProfile r_profile;


	@Container(contentTypes = { ConductancePulse.class, ConductanceStep.class, ConductanceNoise.class }, tag = "components of the profile")
	public ArrayList<ProfileFeature> features = new ArrayList<ProfileFeature>();



	private ConductanceProfile dfltProfile;

	public void add(Object obj) {
		if (obj instanceof GProfileFeature) {
			features.add((GProfileFeature)obj);

		} else if (obj instanceof TimeSeries) {
			setTimeSeries((TimeSeries)obj);

		} else {
			E.error("cant add " + obj);
		}
	}


	public String getAt() {
		return at;
	}

	public ConductanceProfile getConductanceProfile() {
		ConductanceProfile ret = null;
		if (r_profile != null) {
			ret = r_profile;
		} else {
			if (dfltProfile == null) {
				dfltProfile = new ConductanceProfile();

				dfltProfile.setStart(hold);

				for (ProfileFeature f : features) {
					dfltProfile.add(f);
				}
			}
			ret = dfltProfile;
		}
		ret.setStepStyle(getStepStyle());
		ret.setRecordable(getRecordable());
		if (timeSeries != null) {
			ret.setTimeSeries(timeSeries);
		}

		return ret;
	}


	public double getDimlessPotential() {
		return CalcUnits.getVoltageValue(potential);
	}

}
