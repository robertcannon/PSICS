package org.psics.model.synapse;

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
import org.psics.num.model.synapse.TableSynapse;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.phys.Conductance;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.units.Units;


@ModelType(standalone=true, usedWithin={},
		tag="A synapse", info = "")
public class Synapse implements AddableTo, Standalone, LongNamed {

	@TextForm(pos=-1, label="", ignore="")
	@Identifier(tag="Identifier (name) for the synapse type; unique within the model")
	public String id;

	public String info;

	@TextForm(pos=2, label="single channel conductance", ignore="")
	@Quantity(range = "(0.1, 100)", required = true, tag = "Default peak conductance. This conductances " +
			"for synapses in a population can vary according to the weight distribution.", units = Units.pS)
	public Conductance baseConductance;

	 
 
	@ReferenceByIdentifier(location=Location.indirect, required = true, tag = "The permeant ion", targetTypes = { Ion.class })
	public String permeantIon;
	public Ion r_permeantIon;
 
	
	public SynapticTimecourse timecourse;
	
 
	public String getID() {
		return id;
	}


	public void add(Object obj) {
		if (obj instanceof SynapticTimecourse) {
			timecourse = (SynapticTimecourse)obj;
		} else {
			E.warning("cant add " + obj);
		}
	}


	public String getLongName() {
		return "synapse";
	}

 

	public String getPermeantIonID() {
		return permeantIon;
	}


	public TableSynapse tablify() {
		TableSynapse ret = new TableSynapse(id);
	
		timecourse.applyTo(ret);
		 
		ret.normalize();
		ret.setBaseConductance(baseConductance);
		return ret;
	}





 

}
