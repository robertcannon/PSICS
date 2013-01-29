package org.psics.model.channel;

import org.psics.be.E;
import org.psics.be.Exampled;
import org.psics.be.IDd;
import org.psics.num.CalcUnits;
import org.psics.num.model.channel.TransitionType;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.Location;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.units.Units;

public abstract class KSTransition implements Exampled, IDd {

	@Identifier(tag="Identifier (name) for the transition; unique within this channel")
	public String id;

	@ReferenceByIdentifier(tag="The originating state of the transition",
				targetTypes={ClosedState.class, OpenState.class}, required=true, location=Location.local)
	public String from;
	public KSState r_from;

	@ReferenceByIdentifier(tag="The destination state of the transition",
			targetTypes={ClosedState.class, OpenState.class}, required=true, location=Location.local)
	public String to;
	public KSState r_to;

	@Quantity(range = "(5,40)", required = false, tag = "Base temperature for Q10", units = Units.Celsius)
	public Temperature baseTemperature;

	@Quantity(range = "(0,4)", required = false, tag = "Temperature dependence of rates: rate " +
			"change for a rise of ten degrees", units = Units.none)
	public NDValue q10;

	
	
	
    // some data cached here for convenience while resolving;
	private boolean p_done = false;

	private String sysID;

	
	public String getID() {
		return id;
	}
	

	public void setSysID(String s) {
		String wk = s;
		wk = wk.replaceAll("-", "_");
		wk = wk.replaceAll(" ", "_");
		sysID = wk;
	}

	public String getSysID() {
		if (sysID == null) {
			E.error("null sysid in transition " + getClass().getName() + " " + hashCode());
		}
		return sysID;
	}

	public void writeTempDependence(double[] a) {
	if (baseTemperature != null && q10 != null) {
		 a[0] = CalcUnits.getTemperatureValue(baseTemperature);
		 a[1] = q10.getValue();
	 } else {
		 a[0] = 0.;
		 a[1] = 0.;
	 }
	}

	public void copyTemperatureTo(KSTransition ret) {
		if (q10 != null) {
			ret.q10 = q10.makeCopy();
		}
		if (baseTemperature != null) {
			ret.baseTemperature = baseTemperature.makeCopy();
		}
	}


	public boolean linkStates() {
		boolean ret = true;
		if (r_from != null && r_to != null) {
			r_from.addTransition(this);
			r_to.addTransition(this);

			ret = true;
		} else {
			ret = false;
		}
		return ret;
	}




	protected boolean done() {
		return (p_done);
	}

	protected KSState getFrom() {
		return r_from;
	}

	protected KSState getTo() {
		return r_to;
	}

	public String getFromName() {
		return r_from.getID();
	}

	public String getToName() {
		return r_to.getID();
	}


	public void setUndone() {
		p_done = false;
	}


	public void setDone() {
		p_done = true;
	}


	public abstract TransitionType getTransitionType();


	public abstract double[] getTransitionData();




	public String printStructure() {
		return r_from.getID() + "<-->" + r_to.getID();
	}




	public boolean isFromTo(KSState sa, KSState sb) {
		boolean ret = false;
		if (r_from == sa && r_to == sb) {
			ret = true;
		}
		return ret;
	}

	public void setEnds(KSState sa, KSState sb) {
		from = sa.getID();
		r_from = sa;
		to = sb.getID();
		r_to = sb;
		linkStates();
	}


	public abstract KSTransition makeCopy(KSState state, KSState state2);

	public abstract KSTransition makeMultiCopy(KSState state, KSState state2, double ff, double fr);

	// normally makeCopy goes as deeps as there is, but not for coded transitions
	public KSTransition deepCopy(KSState sa, KSState sb) {
		return makeCopy(sa, sb);
	}

	public void setFrom(String s) {
		from = s;
	}

	public void setTo(String s) {
		to = s;
	}

	public void setQ10(double f, Temperature t) {
		baseTemperature = t.makeCopy();
		q10 = new NDValue(f, Units.none);
	}


}
