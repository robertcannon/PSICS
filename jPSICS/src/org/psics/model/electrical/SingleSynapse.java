package org.psics.model.electrical;

import org.psics.be.E;
import org.psics.distrib.DistribPopulation;
import org.psics.distrib.PopulationConstraint;
import org.psics.model.synapse.Synapse;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.ReferenceByLabel;


public class SingleSynapse {
	
	
	@Identifier(tag = "optional identifier for use if access is needed to the parameters of the population")
	public String id;
	
	@ReferenceByLabel(local = false, required = false, tag = "The attachment point on the morphology. The value should" +
	"match the id of the points on the structure where it is to be located.")
	public String at;

	
	@ReferenceByIdentifier(tag="The synapse type",
			targetTypes={Synapse.class}, required=true, location=Location.global)
	public String synapse;
	public Synapse r_synapse;
 
	
	
	
	public Synapse getSynapse() {
		return r_synapse;
	}

	public DistribPopulation makeDistribPopulation() {
		DistribPopulation ret = new DistribPopulation(id, r_synapse.getID());
		ret.setSingle();
		ret.setPointID(at);
		return ret;
	}

 
}
