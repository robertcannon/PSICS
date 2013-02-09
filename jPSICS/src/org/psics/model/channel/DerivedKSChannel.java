package org.psics.model.channel;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.Standalone;
import org.psics.model.ModelMap;
import org.psics.model.ParameterChange;
import org.psics.quantity.DimensionalQuantity;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.units.Units;

@ModelType(info = "The derived channel type allows the creation of a family of channels that differ only in " +
		"one or a few parameter values. It is most useful with large channel definitions that contain code " +
		"fragments. The more compact parameterzed definitions can simply be duplicated without introducing too " +
		"much redundancy.",
		standalone = true,
		tag = "A channel definied through parameter changes from another model", usedWithin = {})
public class DerivedKSChannel extends BaseChannel implements AddableTo, Standalone {

	@Identifier(tag="Identifier (name) for the channel; unique within the model")
	public String id;

	@ReferenceByIdentifier(tag="The original channel definition from which this is derived",
			targetTypes={ClosedState.class, OpenState.class}, required=true, location=Location.global)
	public String from;
	public KSChannel r_from;

	@Container(contentTypes = { ParameterChange.class }, tag = "Changes to the original model that produce the new channel")
	public ArrayList<ParameterChange> changes = new ArrayList<ParameterChange>();

	private KSChannel naturalKSChannel;


	public void add(Object obj) {
		if (obj instanceof ParameterChange) {
			changes.add((ParameterChange)obj);
		} else {
			E.error("cant add " + obj);
		}
	}


	public String getID() {
		return id;
	}


	public KSChannel getNaturalKCShannel() {
		if (naturalKSChannel == null) {
			naturalKSChannel = makeNaturalKSChannel();
		}
		return naturalKSChannel;
	}


	private KSChannel makeNaturalKSChannel() {
		r_from.checkResolved();
		KSChannel ret = r_from.deepCopy();
		ret.id = id;

		ModelMap mm = ModelMap.buildMap(ret);

		for (ParameterChange pc : changes) {
			String tgtid = pc.getTargetID();
			String att = pc.getAttributeName();

			String ref = tgtid + ":" + att;

			// TODO may not always be a ndvalue
			if (mm.hasItem(ref)) {
				DimensionalQuantity val = mm.getQuantity(ref);
				val.setValue(Double.parseDouble(pc.getNewText()), Units.none);

			} else {
				mm.printAvailableSimple();
				E.error("Can't apply parameter change - no such id in model: " + ref);
			}

		}
		ret.resolve();
		return ret;
	}








}
