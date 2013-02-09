package org.psics.model.iaf;

 

	import org.psics.model.channel.KSChannel;
import org.psics.model.synapse.Synapse;
	import org.psics.quantity.annotation.Location;
	import org.psics.quantity.annotation.ModelType;
	import org.psics.quantity.annotation.ReferenceByIdentifier;
	import org.psics.quantity.phys.IntegerQuantity;
import org.psics.quantity.annotation.IntegerNumber;

	@ModelType(info = "A population of synapses for an IaF cell. The synapse " +
			"reference can point to a conductance based or current " +
			"based synapse model", standalone = false, 
			tag = "Population of synapse for an IaFCell", usedWithin = { IaFCell.class })
	public class SynapseSet {
		
		@ReferenceByIdentifier(tag="The synapse type",
				targetTypes={Synapse.class}, required=true, location=Location.global)
		public String synapse;
		public Synapse r_synapse;

		@IntegerNumber(range = "[0, 10^6)", required = false, tag = "Total number of channels in the population")
		public IntegerQuantity number;
	 
		
}
