package org.psics.model.channel;

import java.util.ArrayList;

import org.psics.be.Exampled;
import org.psics.be.IDd;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;

public abstract class KSState implements IDd, Exampled {

	@Identifier(tag="Identifier (name) for the state; unique within this channel")
	public String id;

	@Quantity(range = "(-1, 1)", required = false, tag = "position for visualization of state diagram", units = Units.none)
	public NDValue x;

	@Quantity(range = "(-1, 1)", required = false, tag = "position for visualization of state diagram", units = Units.none)
	public NDValue y;


	// cache of position in a transition table while exporting;
	private int p_iwork;
	private ArrayList<KSTransition> r_transitions = new ArrayList<KSTransition>();


	public KSState() {
	}

	public KSState(String s) {
		id = s;
	}


	public String getID() {
		return id;
	}


	protected void addTransition(KSTransition transition) {
		r_transitions.add(transition);

	}


	public abstract double getRelativeConductance();


	public void setWork(int i) {
		 p_iwork = i;
	}

	public int getWork() {
		return p_iwork;
	}

	public abstract KSState deepCopy();

	public void copyInto(KSState ret) {
		 ret.id = id;
		 if (x != null) {
			 ret.x = x.makeCopy();
		 }
		if (y != null) {
			ret.y = y.makeCopy();
		}
	}


}
