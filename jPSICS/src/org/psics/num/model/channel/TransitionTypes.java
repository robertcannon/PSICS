package org.psics.num.model.channel;

import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.quantity.phys.Charge;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.phys.Phys;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.phys.Temperature;
import org.psics.quantity.phys.Time;
import org.psics.quantity.phys.Voltage;
import org.psics.quantity.units.Units;

public class TransitionTypes {


	public static final int FIXED_RATE = 0;
	public static final int BOLTZMANN_VDEP = 1;
	public static final int EXP_LINEAR_ONE_WAY = 2;
	public static final int BINDING = 3;
	public static final int EXP_ONE_WAY = 4;
	public static final int SIGMOID_ONE_WAY = 5;
	public static final int CODED = 6;
	public static final int FUNCTION = 7;


	static Temperature tCache = null;
	static double onebyktCache;


	static double onebykt = Double.NaN;    // no default temperature - must be set explicitly;



	public static void setTemperature(Temperature t) {
		onebykt = evalOneByKt(t);
	}

	static double evalOneByKt(Temperature t) {
		double ret = 0.;
		if (t == tCache) {
			ret = onebyktCache;
		} else {
			PhysicalQuantity kt = Phys.BOLTZMANN_CONSTANT.times(t);
			ret = 1. / (kt.getValue(Units.meV));
		}
		return ret;
	}



public static double expLinearForwardRate(double v, double[] d) {
	// d contains, rate, vInflection, vscale

	// rate = A x / (1 - exp(-x))
	// wher x = (V - V0) / B

	double x =  (v - d[3]) / d[4];
	double ret = 0.;
	if (Math.abs(x) > 1.e-5) {
		ret = d[2] * x / (1- Math.exp(-x));
	} else {
		ret = d[2];
	}
	return ret;
}

public static double expForwardRate(double v, double[] d) {
	// d contains, rate, vInflection, vscale

	// rate = A exp(x)
	// wher x = (V - V0) / B

	double x =  (v - d[3]) / d[4];
	double ret = d[2] * Math.exp(x);
	return ret;
}


public static double sigmoidForwardRate(double v, double[] d) {
	// d contains, rate, vInflection, vscale

	// rate = A / ( 1 + exp(-x))
	// wher x = (V - V0) / B

	double x =  (v - d[3]) / d[4];
	double ret = d[2] / ( 1 + Math.exp(-x));
	return ret;
}





   public static double boltzmannForwardRate(double v, double[] d) {
         double a = d[2] * Math.exp(onebykt * d[6] * d[7] * v);
         double ret = 1. / (1. / a + 1. / d[4]);
         ret *= d[8];
      return ret;
   }


   public static double boltzmannReverseRate(double v, double[] d) {
         double b = d[3] * Math.exp(-onebykt * d[6] * (1. - d[7]) * v);
         double ret = 1. / (1. / b + 1. / d[5]);
         ret *= d[9];
      return ret;
   }



public static final double[] ratesOfZVGTTT (Charge xz, Voltage xvh, NDValue xgamma,
				      Time xtau, Time xtaudragfwd, Time xtaudragrev, double fwdFactor, double revFactor) {

	double z = xz.getValue(Units.e);
	double vh = CalcUnits.getVoltageValue(xvh);
	double tau = CalcUnits.getTimeValue(xtau);
	double taudragfwd = CalcUnits.getTimeValue(xtaudragfwd);
	double taudragrev = CalcUnits.getTimeValue(xtaudragrev);
	double gamma = xgamma.getValue(Units.none);

	if (tau < 1.e-6) {
		E.error("zero tau in v half transition?" + tau);
	}

    double rf = Math.exp (-onebykt * gamma * z * vh) / tau;
    double rr = Math.exp (onebykt * (1-gamma) * z * vh) / tau;
    double rmf =  (taudragfwd <= 0.0 ? 1.e6 : 1./taudragfwd);
    double rmr =  (taudragrev <= 0.0 ? 1.e6 : 1./taudragrev);
    double[] dd = {rf, rr, rmf, rmr, z, gamma, fwdFactor, revFactor};
    return dd;
}


public static final double[] vhEtcOfRRRRZG (double rf, double rr,
		double rmf, double rmr, double z, double gamma) {

   double tf = 1./ rf;
   double tr = 1./ rr;
   /* vh is such that
 exp (ebykt * z * g * vh)/tf = exp (ebykt * z * (1-g) * -vh) / tr;
 exp(ebykt * z * vh) = tf / tr;
 vh = (log (tf) - log (tr) ) / (ebykt * z);
*/
   double vh = 0.0;
   if (z != 0.0) vh = (Math.log(tf) - Math.log (tr)) / (onebykt * z);
   double tx = tf / Math.exp (onebykt * z * gamma * vh);
   double tmf = 1. / rmf;
   double tmr = 1. / rmr;
   double[] dd = {z, vh, gamma, tx, tmf, tmr};
   return dd;
}



@SuppressWarnings("unused")
static final double alpha (double v, double rf, double rr, double rm,
                            double z, double gamma) {
   double a = rf * Math.exp (onebykt * (gamma) * z * v);
   a = 1. / (1. / a + 1./rm);
   return a;
}

@SuppressWarnings("unused")
static final double beta  (double v, double rf, double rr, double rm,
                            double z, double gamma) {

   double b = rr * Math.exp (-onebykt * (1. - gamma) * z * v);
   b = 1. / (1. / b + 1./rm);
   return b;
}


static final double XinfOfV (double v, double rf, double rr, double rm,
				double z, double gamma) {

   double a = alpha(v, rf, rr, z, gamma, rm);
   double b = beta (v, rf, rr, z, gamma, rm);
   return a / (a + b);
}



static final double TauOfV (double v, double rf, double rr, double rm,
			       double z, double gamma) {

   double a = alpha (v, rf, rr, z, gamma, rm);
   double b = beta (v, rf, rr, z, gamma, rm);
   return 1. / (a + b);
}


}

