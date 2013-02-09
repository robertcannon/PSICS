package org.psics.model.stimrec;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.phys.Current;
import org.psics.quantity.units.Units;

@ModelType(info = "A current clamp for attaching to a cell. The 'at' attribute should match the id of" +
		" a point on the cell.", standalone = false, tag = "Current clamp", usedWithin = { Access.class })
public class CurrentClamp extends Clamp implements AddableTo {


	@Quantity(range = "", required = false, tag = "Initial holding current", units = Units.nA)
	public Current hold;


	// could want this local
	@ReferenceByIdentifier(location = Location.global, required = false, tag = "", targetTypes = { CurrentProfile.class })
	public String profile;
	public CurrentProfile r_profile;


	@Container(contentTypes = { CurrentPulse.class, CurrentStep.class, CurrentNoise.class }, tag = "components of the profile")
	public ArrayList<ProfileFeature> features = new ArrayList<ProfileFeature>();

	private CurrentProfile dfltProfile;



	public void add(Object obj) {
		if (obj instanceof CProfileFeature) {
			features.add((CProfileFeature)obj);

		} else if (obj instanceof TimeSeries) {
			setTimeSeries((TimeSeries)obj);

		} else {
			E.error("cant add " + obj);
		}
	}


	public String getAt() {
		return at;
	}

	public CurrentProfile getCurrentProfile() {
		CurrentProfile ret = null;
		if (r_profile != null) {
			ret = r_profile;
		} else {
			if (dfltProfile == null) {
				dfltProfile = new CurrentProfile();

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

}
