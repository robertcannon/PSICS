package org.psics.model.electrical;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.distrib.DistribPopulation;
import org.psics.distrib.PopulationConstraint;
import org.psics.model.channel.BaseChannel;
import org.psics.model.channel.DerivedKSChannel;
import org.psics.model.channel.KSChannel;
import org.psics.model.synapse.Synapse;
import org.psics.model.synapse.SynapticWeights;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Expression;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.StringEnum;
import org.psics.quantity.phys.IntegerQuantity;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.phys.SurfaceNumberDensity;
import org.psics.quantity.phys.SurfaceNumberDensityExpression;
import org.psics.quantity.units.Units;

public class SynapsePopulation extends Population {
	
	
	@ReferenceByIdentifier(tag="The synapse type",
			targetTypes={Synapse.class}, required=true, location=Location.global)
	public String synapse;
	public Synapse r_synapse;

	
	
	
	public SynapticWeights weights;
	
	
	@Override
	public String getTargetID() {
		 return synapse;
	}

	@Override
	public void setTargetID(String sid) {
		synapse = sid;
	}
	
	
	@Override
	public boolean subAdd(Object obj) {
		boolean ret = false;
		if (obj instanceof SynapticWeights) {
			weights = (SynapticWeights)obj;
			ret = true;
		}
		return ret;
	}
	
	
	public Synapse getSynapse() {
		return r_synapse;
	}
	
}
