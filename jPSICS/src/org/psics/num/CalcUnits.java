package org.psics.num;

import org.psics.be.E;
import org.psics.quantity.phys.BulkResistivity;
import org.psics.quantity.phys.Capacitance;
import org.psics.quantity.phys.Charge;
import org.psics.quantity.phys.Current;
import org.psics.quantity.phys.Frequency;
import org.psics.quantity.phys.Length;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.phys.Resistance;
import org.psics.quantity.phys.SurfaceCapacitance;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;


public class CalcUnits {


	private final static Units time = Units.ms;
	private final static Units length = Units.um;
	private final static Units current = Units.pA;
	private final static Units capacitance = Units.pF;
	private final static Units voltage = Units.mV;
	private final static Units conductance = Units.nS;
	private final static Units resistance = Units.Gohm;
	private final static Units resistivity = Units.Gohm_um;
	private final static Units reciprocolArea = Units.per_um2;
	private final static Units specificCapacitance = Units.pF_per_um2;
	private final static Units charge = Units.e;
	private final static Units temperature = Units.K;
	private final static Units frequency = Units.per_ms;


	public static void main(String[] argv) {
		E.info("Calculation units consistency check - all result factors should be 1.0");
		Voltage v = new Voltage(1., Units.V);
		Resistance r = new Resistance(1., Units.ohm);
		Current c = new Current(1., Units.A);
		E.info("V / (I R) " + v.getValue(voltage) / (c.getValue(current) * r.getValue(resistance)));

		Time t = new Time(1., Units.s);
		Capacitance cap = new Capacitance(1., Units.F);
		E.info("I / (C dV/dt) " + c.getValue(current) / (cap.getValue(capacitance) * v.getValue(voltage) / t.getValue(time)));

		Length l = new Length(1., Units.m);

		SurfaceCapacitance cspec = new SurfaceCapacitance(1., Units.F_per_m2);
		E.info("Cspec * area / C " + cspec.getValue(specificCapacitance) * l.getValue(length) * l.getValue(length) / cap.getValue(capacitance));


		BulkResistivity br = new BulkResistivity(1., Units.ohm_m);
		E.info("R L / resistivity " + r.getValue(resistance) * l.getValue(length) / br.getValue(resistivity));
	}



	public static double getV(PhysicalQuantity pq, Units u) {
		return getV(pq, u, 0.);
	}

	public static double getV(PhysicalQuantity pq, Units u, double dflt) {
		double ret = dflt;
		if (pq != null) {
			ret = pq.getValue(u);
		}
		return ret;
	}


	public static double getConductanceValue(PhysicalQuantity pq) {
		return getV(pq, conductance);
	}

	public static double getCurrentValue(PhysicalQuantity pq) {
		return getV(pq, current);
	}

	public static double getTimeValue(PhysicalQuantity pq) {
		return getV(pq, time);
	}

	public static double getVoltageValue(PhysicalQuantity pq) {
		 return getV(pq, voltage);
	}


	public static double getLengthValue(PhysicalQuantity pq) {
		 return getV(pq, length);
	}

	public static double getResistivityValue(PhysicalQuantity pq) {
		return getV(pq, resistivity);
	}

	public static double getSpecificCapacitance(PhysicalQuantity pq) {
		return getV(pq, specificCapacitance);
	}


	public static double getTemperatureValue(PhysicalQuantity pq) {
		 // ADHOC
		 double t0 = 273.15;
		 double tempc = getV(pq, temperature, t0);
		 if (tempc > t0) {
			 	tempc -= t0;
		 }
		 if (tempc < 5 || tempc > 40.) {
			 E.warning("Possible temperature range error? " + tempc);
		 }
		 return tempc;
	}

	public static double getReciprocalArea(PhysicalQuantity pq) {
		return getV(pq, reciprocolArea);
	}




	public static Time newTime(double d) {
		return new Time(d, time);
	}

	public static Voltage newVoltage(double d) {
		return new Voltage(d, voltage);
	}



	public static Charge newCharge(double d) {
		return new Charge(d, charge);
	}



	public static int getInt(NDNumber seed) {
		int ret = -1;
		if (seed != null) {
			ret = seed.getNativeValue();
		}
		return ret;
	}


	public static Length makeLength(double d) {
		 return new Length(d, length);
	}


	public static double getFrequencyValue(PhysicalQuantity pq) {
		return getV(pq, frequency);
	}

}
