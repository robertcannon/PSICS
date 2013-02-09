package org.psics.model.electrical;

import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

@ModelType(info = "A density adjustment can be used to implement the effects of a hypothetical " +
		"homeostatic process that adjusts one or more channel densities to maintain a " +
		"uniform resting potential ", standalone = false,
		tag = "Channel density adjustment to establish rest potential", usedWithin = { CellProperties.class })
public class DensityAdjustment {


	@Quantity(range = "(-80, -50)", required = false, tag = "The membrane potential at which currents should balance", units = Units.mV)
	public Voltage maintain;


	@Label(info = "This should be a comma separated list of one or two channel id. If only one channel" +
			"is listed, then its density is adjusted, where possible, to match the desired rest potential. If " +
			"two are listed, then both are adjusted to acieve the desired equlibrium and maintain the same " +
			"conductance density.", tag = "Channel types to be used to balance the current")
	public String vary;


	public Voltage getTargetVoltage() {
		return maintain;
	}

	public String[] getVariableChannels() {
		String[] sa = vary.split(",");
		for (int i = 0; i < sa.length; i++) {
			sa[i] = sa[i].trim();
		}
		return sa;
	}

}
