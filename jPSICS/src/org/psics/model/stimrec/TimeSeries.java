package org.psics.model.stimrec;
import org.psics.be.DataFileSourced;
import org.psics.be.E;
import org.psics.num.CalcUnits;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceToFile;
import org.psics.quantity.phys.AnyQuantity;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.phys.Time;
import org.psics.quantity.units.Units;

@ModelType(info = "", standalone = false, tag = "tiem series data from a file",
		usedWithin = { CurrentClamp.class, ConductanceClamp.class, VoltageClamp.class })
public class TimeSeries implements DataFileSourced {

	@Identifier(tag = "optional identifier for use if the feature is to be modified")
	public String id = "";

	@ReferenceToFile(tag = "name of the file with time series data",
			fallback = "", required = true)
	public String file;


	@IntegerNumber(range = "[1, 1000)", required = false, tag = "(optional) column to use if " +
			"there are more than two columns (column 0 is always the time) in the file")
	public NDNumber column;


	@Quantity(range = "[0, 1000)", required = true, tag = "time unit",
			units = Units.ms)
	public Time timeUnit = new Time();


	@Quantity(range = "[0, 1000)", required = false, tag = "base unit for the value column",
			units = Units.any)
	public AnyQuantity valueUnit = new AnyQuantity(0., Units.any);


	@Quantity(range = "[0, 1000)", required = false, tag = "Optional scale factor to for modifying the " +
			"amplitude of the profile in, for example, a parameter sweep",
			units = Units.none)
	public NDValue scaleFactor = new NDValue(1.);

	double[][] data;



	public String getID() {
		return id;
	}


	public String getFileName() {
		return file;
	}


	public void setData(double[][] da) {
		data = da;
	}

	
	public double[] getNormalizedFlattenedData(PhysicalQuantity pq) {
		double vf = 1.;

		if (pq.compatibleWith(valueUnit)) {
			vf = getCalcUnitsValueFactor(valueUnit);

		} else {
			E.error("value unit in time series must have dimensions that match " + pq);
			vf = 1;
		}
		vf *= scaleFactor.getValue();

		if (data == null) {
			E.error("no data in TimeSeries");
			return new double[0];
		}

		int nl = data.length;
		int ncol = data[0].length;

		double tf = getToCalcUnitsTimeFactor();

		int icol = 1;
		if (column != null) {
			icol = column.getValue();
			if (icol == 0) {
				E.warning("TimeSeries referes to column 0, but this should be the times");
				icol = 1;
			} else if (icol >= ncol) {
				E.warning("TimeSeries revers to nonexistent column " + icol + " The last one " +
						" is " + (ncol - 1));
				icol = ncol - 1;
			}
		}
		
		int nco = 2; // number of columns actually returned
		double[] ret = new double[nl * nco];
		for (int i = 0; i < nl; i++) {
			ret[nco * i] = data[i][0] * tf;
			ret[nco * i + 1] = data[i][icol] * vf;
			/*
			for (int j = 1; j < nco; j++) {
				ret[nco * i + j] = data[i][j] * vf;
			}
			*/
		}
		return ret;
	}


	public double getToCalcUnitsTimeFactor() {
		double d = CalcUnits.getTimeValue(timeUnit);
		return d;
	}


	public double getCalcUnitsValueFactor(PhysicalQuantity pq) {
		double mag = 0.;
		if (pq.compatibleWith(Units.nA)) {
			mag = CalcUnits.getCurrentValue(pq);

		} else if (pq.compatibleWith(Units.mV)) {
			mag = CalcUnits.getVoltageValue(pq);

		} else if (pq.compatibleWith(Units.nS)) {
			mag = CalcUnits.getConductanceValue(pq);

		} else {
			E.error("unrecognized quantity " + pq);
		}
		if (mag == 0.) {
			E.error("zero unit value for time series data?");
			mag = 1;
		}
		return mag;
	}

}
