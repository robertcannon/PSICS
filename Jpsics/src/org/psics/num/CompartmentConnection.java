package org.psics.num;

import org.psics.be.E;



public class CompartmentConnection {


	double area;
	double couplingFactor;

	double capacitance;
	double conductance = Double.NaN; // temp

	Compartment[] ends;

	Compartment to;
	Compartment from;
	public double workTo;


	public CompartmentConnection(Compartment c1, Compartment c2,
				double cf, double ar) {
		ends = new Compartment[2];
		ends[0] = c1;
		ends[1] = c2;
		couplingFactor = cf;
		area = ar;
	}



	public void setResistivity(double res) {
		conductance = couplingFactor / res;
	}

	public double getConductance() {
		return conductance;
	}

	public double getArea() {
		return area;
	}


	public int otherIndex(Compartment c) {
		 int ret = -1;
		 if (c == ends[0]) {
			 ret = ends[1].getIndex();
		 } else {
			 ret = ends[0].getIndex();
		 }
		 return ret;
	}


	public void orderTo(Compartment cpt) {
		to = cpt;
		from = otherEnd(to);
	}


	public void orderFrom(Compartment cpt) {
		from = cpt;
		to = otherEnd(cpt);
	}


	private Compartment otherEnd(Compartment c) {
	    Compartment ret = null;
		if (c == ends[1]) {
			ret = ends[0];
		} else if (c == ends[0]) {
			ret = ends[1];
		} else {
			E.error("ordering to a non-end?");
		}
		return ret;
	}


	public Compartment getOther(Compartment c) {
		return otherEnd(c);
	}



}
