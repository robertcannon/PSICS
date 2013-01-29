package org.psics.model.control;

import org.psics.num.Discretization;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;



@ModelType(standalone=false, usedWithin={PSICSRun.class},
		tag="Discratization parameters for channels", info="Channel transition rates are tabulated for use in the " +
				"calculation. A discretization specification can be used to change the default table range and step. " +
				"It is unlikely to be useful to set this except for detailed performance or accuracy tests.")
public class ChannelDiscretization {

	@Quantity(range = "(-100., -70.0)", required=false, tag="Minimum tabulated potential",
			units = Units.mV)
	public Voltage vMin;


	@Quantity(range = "(20., -60.0)", required=false, tag="Maximum tabulated potential",
			units = Units.mV)
	public Voltage vMax;


	@Quantity(range = "(0.3, 3.0)", required=false, tag="Voltage step for linearization of channel tables",
			units = Units.mV)
	public Voltage deltaV;




	public Discretization getDiscretization() {
		Discretization ret = new Discretization(vMin, vMax, deltaV);
		return ret;
	}


}
