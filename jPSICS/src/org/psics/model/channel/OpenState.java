package org.psics.model.channel;

import org.psics.be.Exampled;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;


@ModelType(standalone=false, usedWithin={KSChannel.class},
		tag="Permeable configuration of an ion channel", info="The " +
		"conductance is specified relative to the maximum conductance for the " +
		"channel. Different states can have different conductances, but other " +
		"permeation properties are assumed to be the same for all open states.")
public class OpenState extends KSState implements Exampled {


	@Quantity(units=Units.none, range="(0, 1)", tag="relative conductance for this state " +
			"compared to the channel conductance", required=true)
	public NDValue gRel = new NDValue(1.0);


	public OpenState() {
	}

	public OpenState(String s) {
		super(s);
	}

	public OpenState(double d) {
		gRel.setValue(d, Units.none);
	}

	public OpenState(String s, double d) {
		super(s);
		gRel.setValue(d, Units.none);
	}

	public double getRelativeConductance() {
		return gRel.getNativeValue();
	}

	public String getExampleText() {
		return "<OpenState id=\"O\" grel=\"1.0\"/>";
	}

	public void setRelativeConductance(double d) {
		gRel.setValue(d, Units.none);

	}

	public OpenState deepCopy() {
		OpenState ret = new OpenState();
		ret.gRel = gRel.makeCopy();
		super.copyInto(ret);
		return ret;
	}
}
