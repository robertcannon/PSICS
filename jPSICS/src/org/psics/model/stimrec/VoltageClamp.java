package org.psics.model.stimrec;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

@ModelType(info = "A voltage clamp for attaching to a cell. The 'at' attribute should match the id of" +
		" a point on the cell. If general clamp recording is on, or the specific clamp has its " +
		"<x>record</x> flag set then the <b>channel current</b> on the target compartment is recorded. " +
		"Note that this is <b>not the total current</b> through the clamp: it ignores capacitative currents and, " +
		"for extended cells, current flows into neighbouring parts of the structure. At present, the best " +
		"way to find the total clamp current is to use an imperfect clamp implemented as a <x>ConductanceClamp</x> with" +
		"a high conductance. This will cause the target point to deviate slightly from the applied voltage. The deviation can" +
		"be used with the known conductance to compute the current flow through the clamp. By using a large" +
		"conductance you can get the cell to follow the clamp potential arbitrarily closely but the accuracy of " +
		"the derived current will decrease as the potential difference becomes very small.", 
		standalone = false, tag = "Current clamp", usedWithin = { Access.class })
public class VoltageClamp extends Clamp implements AddableTo {


	@Quantity(range = "", required = false, tag = "", units = Units.mV)
	public Voltage hold;


	// could want this local
	@ReferenceByIdentifier(location = Location.global, required = false, tag = "", targetTypes = { VoltageProfile.class })
	public String profile;
	public VoltageProfile r_profile;

	@Container(contentTypes = { VoltagePulse.class, VoltageStep.class, VoltageNoise.class }, tag = "components of the profile")
	public ArrayList<ProfileFeature> features = new ArrayList<ProfileFeature>();


	private VoltageProfile dfltProfile;

	public void add(Object obj) {
		if (obj instanceof VProfileFeature) {
			 features.add((VProfileFeature)obj);

		} else if (obj instanceof TimeSeries) {
			setTimeSeries((TimeSeries)obj);

		} else {
			E.error("cant add " + obj);
		}
	}


	public String getAt() {
		return at;
	}

	public VoltageProfile getVoltageProfile() {
		VoltageProfile ret = null;
		if (r_profile != null) {
			ret = r_profile;

		} else {
			if (dfltProfile == null) {
				dfltProfile = new VoltageProfile();

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

