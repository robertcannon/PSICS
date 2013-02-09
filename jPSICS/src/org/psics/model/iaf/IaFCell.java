package org.psics.model.iaf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.psics.be.AddableTo;
import org.psics.be.ContainerForm;
import org.psics.be.E;
import org.psics.be.LongNamed;
import org.psics.be.Standalone;
import org.psics.be.TextForm;
import org.psics.model.control.About;
import org.psics.model.environment.Ion;
import org.psics.model.math.Function;
import org.psics.num.model.channel.TableChannel;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.phys.Capacitance;
import org.psics.quantity.phys.Conductance;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;


@ModelType(standalone=true, usedWithin={},
		info="An Integrate and Fire cell defined by a capacitance, threshold, reset potential" +
				"refractory period and optional leak timescale. Channel populations " +
				"can be included in the cell with conductance, current or charge based synapses. " +
				"Note that the leak timescale, is simply a convenience " +
				"and is equivalent to adding a population of non-gated channels " +
				"with reversal potential Vrest.", tag = "Simple Integrate and Fire cell with a" +
						" capacitance and a threshold")
public class IaFCell implements Standalone {

	@Identifier(tag="Identifier for the cell type; unique within the model")
	public String id;

	@Quantity(range = "(0.1, 100)", required = true, tag = "Capacitance of the cell. IaF cells are " +
			"point-like with surface area. To use the standard channel distribution components " +
			"on an Integrate and Fire cell, use the SphericalIaFCell", units = Units.pF)
	public Capacitance capacitance;

	@Quantity(range = "(-80, -50)", required = true, tag ="Potential to which the cell is " +
			"reset after a spike", units = Units.mV)
	public Voltage reset_potential;
	
	@Quantity(range = "(-60, -20)", required = true, tag ="Threshold for generating a spike.", units = Units.mV)
	public Voltage threshold;
	
	@Quantity(range = "(0.1, 10)", required = false, tag ="After a spike, the membrane potential is held at " +
			"the reset potential for the refractory_period. Synaptic input and channel currents" +
			" have no effect, though ongoing synaptic conductances from events that arrived during the refractory " +
			"period will have an effect once the refractory period expires.", units = Units.ms)
	public Time refractory_period;
	
	@Quantity(range = "(1, 100)", required = false, tag="Convenience parameter for creating a " +
			"leaky cell model. It adds a conductance that reverses at the reset potential" +
			" with magnitude  capacitance / leak_timescale", units=Units.ms) 
	public Time leak_timescale;
	
 
	@Container(tag="channel sets", contentTypes={ChannelSet.class})
	public ArrayList<ChannelSet> c_channelSets = new ArrayList<ChannelSet>();
 
	@Container(tag="synapse sets", contentTypes={SynapseSet.class})
	public ArrayList<SynapseSet> c_synapseSets = new ArrayList<SynapseSet>();

 

	public String getID() {
		// TODO Auto-generated method stub
		return null;
	}

 
 

}
