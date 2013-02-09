package org.psics.quantity.phys;

import org.psics.quantity.units.Units;



public class Phys {
	
	// some magic numbers that occur in physical quantities with different scake factors
	// (without any powers of ten that may be needed);
	
	// to relates units expresed in electron charges to units in Coulombs (Amp seconds)
	public final static double felectron = 1.60217646;
	
	// the floating part of Avagadro's number (Navagadro = favagadro * 1e23)
	public final static double favagadro = 6.02214199;
	
	
	public final static PhysicalQuantity ELECTRON_CHARGE = new PhysicalQuantity(1., Units.e);
	public static final PhysicalQuantity BOLTZMANN_CONSTANT = new PhysicalQuantity(8.617343e-5,  Units.eV_per_K);
		
	 
	 
	
}
