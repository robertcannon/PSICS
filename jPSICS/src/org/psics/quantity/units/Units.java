package org.psics.quantity.units;

import org.psics.quantity.phys.Phys;

/*
 *  Units are defined by their dimensionality in Mass, Length, Time, Ampere, and Kelvin.
 *  (the SI base dimensions without luminance)
 *  The next two arguments are the power of ten, and a numeric factor which applies to those
 *  quantities that are not exact powers of ten of the fundamental ones. A value of zero indicates
 *  that the quantity is an exact power of ten. Then there is a longer name, and, optionally,
 *  one or more expressions relation the unit to other units definitions. These provide
 *  a degree of sanity checking of the dimensions as implemented in the main method.
 *
 *  N.B. as yet, only simple relations in the from "A op B" are supported, and they require
 *  spaces around the operator.
 *
 */
public enum Units {
	none(0, 0, 0, 0, 0,        0, 0, "non dimensional", ""),
	any(0, 0, 0, 0, 0,        0, 0, "not specified", ""),
	matching(0, 0, 0, 0, 0,        0, 0, "matching", ""),
	truefalse(0, 0, 0, 0, 0,        0, 0, "boolean", ""),

	m(0, 1, 0, 0, 0,           0, 0,  "metre", ""),
	cm(0, 1, 0, 0, 0,         -2, 0, "centimetre", "m / 1e2"),
	mm(0, 1, 0, 0, 0,         -3, 0, "milimetre", "m / 1e3"),
	um(0, 1, 0, 0, 0,        -6, 0, "micron", "m / 1e6"),
	nm(0, 1, 0, 0, 0,         -9, 0, "nanometre", "m / 1e9"),

	per_um(0, -1, 0, 0, 0,     6, 0, "per micron", "1 / um"),

	m2(0, 2, 0, 0, 0,           0, 0, "square metres", "m * m"),
	cm2(0, 2, 0, 0, 0,           -4, 0, "square centimetres", "cm * cm"),
	um2(0, 2, 0, 0, 0,           -12, 0, "square microns", "um * um"),
	nm2(0, 2, 0, 0, 0,           -18, 0, "square nanometres", "nm * nm"),
	per_um2(0, -2, 0, 0, 0,     12, 0, "per square micron", "1 / um2"),

	m3(0, 3, 0, 0, 0,           0, 0, "cubic metres", "m * m2"),
	l(0, 3, 0, 0, 0,           -3, 0, "litre", "m3 / 1e3"),

	kg(1, 0, 0, 0, 0,          0, 0, "kilogram", ""),
	g(1, 0, 0, 0, 0,          -3, 0, "gram", "kg / 1e3"),

	s(0, 0, 1, 0, 0,           0, 0, "second", ""),
	ms(0, 0, 1, 0, 0,         -3, 0, "millisecond", "s / 1e3"),
	us(0, 0, 1, 0, 0,        -6, 0, "microsecond", "s / 1e6"),
	ns(0, 0, 1, 0, 0,         -9, 0, "nanosecond", "s / 1e9"),
	min(0, 0, 1, 0, 0,         0, 60., "minute", "s * 60"),
	per_s(0, 0, -1, 0, 0,      0, 0, "per second", "1 / s"),
	per_ms(0, 0, -1, 0, 0,     3, 0, "per millisecond", "1 / ms"),
	Hz(0, 0, -1, 0, 0,         0, 0, "Herz", "per_s"),

	mol(0, 0, 0, 0, 0,        23, Phys.favagadro, "mole", ""),
	per_mol(0, 0, 0, 0, 0,    -23, 1./Phys.favagadro, "per mole", "1 / mol"),
	per_s_per_mol(0, 0, -1, 0, 0, -23, 1./Phys.favagadro, "per second per mole", "per_mol / s"),
	mol_per_l(0, -3, 0, 0, 0,        26, Phys.favagadro, "molarity", "mol / l"),
	M(0, -3, 0, 0, 0,        26, Phys.favagadro, "molarity", "mol / l"),
	mM(0, -3, 0, 0, 0,        23, Phys.favagadro, "molarity", "M / 1.e3"),
	uM(0, -3, 0, 0, 0,        20, Phys.favagadro, "molarity", "M / 1e6"),
	nM(0, -3, 0, 0, 0,        17, Phys.favagadro, "molarity", "M / 1e9"),
	per_s_per_M(0, 3, -1, 0, 0, -26, 1./Phys.favagadro, "per second per mole", "per_s / mol_per_l"),
	per_ms_per_M(0, 3, -1, 0, 0, -29, 1./Phys.favagadro, "per second per mole", "per_s_per_M / 1e3"),
	per_ms_per_mM(0, 3, -1, 0, 0, -32, 1./Phys.favagadro, "per second per mole", "per_s_per_M / 1e6"),
	l_per_s_per_mol(0, 3, -1, 0, 0, -26, 1./Phys.favagadro, "per second per mole", "per_s / mol_per_l"),



	velSI(0, 1, -1, 0, 0,      0, 0, "velocity", "m / s"),
	accSI(0, 1, -2, 0, 0,        0, 0, "acceleration", "velSI / s"),
	N(1, 1, -2, 0, 0,          0, 0, "newton", "kg * accSI"),
	J(1, 2, -2, 0, 0,          0, 0, "joule", "N * m"),

	A(0, 0, 0, 1, 0,           0, 0, "ampere", ""),
	mA(0, 0, 0, 1, 0,          -3, 0, "milliampere", "A / 1e3"),
	uA(0, 0, 0, 1, 0,         -6, 0, "microampere", "mA / 1e3"),
	nA(0, 0, 0, 1, 0,         -9, 0, "nanoampere", "uA / 1e3"),
	pA(0, 0, 0, 1, 0,         -12, 0, "picoampere", "nA / 1e3"),
	C(0, 0, 1, 1, 0,           0, 0, "coulomb", "A * s"),
	V(1, 2, -3, -1, 0,         0, 0, "volt", "J / C"),
	mV(1, 2, -3, -1, 0,       -3, 0, "millivolt", "V / 1e3"),
	KV(1, 2, -3, -1, 0,        3, 0,  "Kilovolt", "V * 1e3"),
	MV(1, 2, -3, -1, 0,        6, 0,  "Megavolt", "V * 1e6"),
	per_mV(-1, -2, 3, 1, 0,    3, 0, "per millivolt", "1 / mV"),

    ohm(1, 2, -3, -2, 0,       0, 0, "ohm", "V / A"),
    kohm(1, 2, -3, -2, 0,       3, 0, "kilohm", "V / mA"),
    Mohm(1, 2, -3, -2, 0,       6, 0, "megohm", "V / uA"),
    ohm_m(1, 3, -3, -2, 0,  0, 0, "ohm metre", "ohm * m"),
    ohm_cm(1, 3, -3, -2, 0,  -2, 0, "ohm centimetre", "ohm * cm"),
    kohm_cm(1, 3, -3, -2, 0,       1, 0, "kilohm centimetre", "kohm * cm"),
    Gohm(1, 2, -3, -2, 0,       9, 0, "gigohm", "V / nA"),
    Gohm_um(1, 3, -3, -2, 0,  3, 0, "gigohm micron", "Gohm * um"),


    W(1, 2, -3, 0, 0,          0, 0, "watt", "V * A"),
    F(-1, -2, 4, 2, 0,         0, 0, "farad", "C / V"),
    uF(-1, -2, 4, 2, 0,       -6, 0, "microfarad", "F / 1e6"),
    nF(-1, -2, 4, 2, 0,       -9, 0, "nanofarad", "uF / 1e3"),
    pF(-1, -2, 4, 2, 0,       -12, 0, "picofarad", "nF / 1e3"),

    S(-1, -2, 3, 2, 0,         0, 0, "siemens", "1 / ohm"),
    uS(-1, -2, 3, 2, 0,         -6, 0, "microsiemens", "S / 1e6"),
    nS(-1, -2, 3, 2, 0,         -9, 0, "nanosiemens", "uS / 1e3"),
    pS(-1, -2, 3, 2, 0,         -12, 0, "picosiemens", "nS / 1e3"),
    S_per_m(-1, -3, 3, 2, 0,       0, 0, "siemens per metre", "S / m"),

    e(0, 0, 1, 1, 0,           -19, Phys.felectron, "electronic charge", "C * 1.60217646e-19"),
    e_per_ms(0, 0, 0, 1, 0,     -16,  Phys.felectron, "charges pre milisecond", "e / ms"),
    eV(1, 2, -2, 0, 0,          -19, Phys.felectron, "electron-volts", "e * V"),
    meV(1, 2, -2, 0, 0,          -22, Phys.felectron, "milli electron volts", "e * mV"),
    MeV(1, 2, -2, 0, 0,          -13, Phys.felectron, "Mega electron volts", "e * MV"),
    eV_per_K(1, 2, -2, 0, -1,   -19, Phys.felectron, "electron volts per Kelvin", "eV / K"),



    F_per_m2(-1, -4, 4, 2, 0,    0, 0, "farads per square metre", "F / m2"),
    uF_per_cm2(-1, -4, 4, 2, 0,    -2, 0, "microfarads per square centimetre", "uF / cm2"),
    uF_per_um2(-1, -4, 4, 2, 0,  6, 0, "microfarads per square micron", "uF / um2"),
    nF_per_um2(-1, -4, 4, 2, 0,  3, 0, "nanofarads per square micron", "nF / um2"),
    pF_per_um2(-1, -4, 4, 2, 0,    0, 0, "picofarads per square micron", "pF / um2"),


	K(0, 0, 0, 0, 1,           0, 0, "Kelvin", ""),
	Celsius(0, 0, 0, 0, 1,           0, 0, "Celsius", "");

	static final double felectron = 1.60217646;


	DimensionSet dset;


	private Units(int m, int l, int t, int a, int k, int qTen, double ff, String sn, String sc) {
		 dset = new DimensionSet(m, l, t, a, k, qTen, ff, sn, sc);
	}


	public DimensionSet getDimensionSet() {
		return dset;
	}


	public static Units getByLabel(String sl) {
		Units ret = null;
		for (Units u : values()) {
			if (u.name().equals(sl)) {
				ret = u;
			}
		}
		return ret;
	}


	public String getName() {
		return name();
	}

	public static void main(String[] argv) {
		for (Units u : values()) {
			u.dset.checkRelations();
		}
	}


	public double getToConversionFactor(Units u) {
		 return dset.getToConversionFactor(u.dset);
	}


}
