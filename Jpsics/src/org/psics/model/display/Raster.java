package org.psics.model.display;

import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceToFile;
import org.psics.quantity.phys.NDNumber;
import org.psics.quantity.phys.NDValue;
import org.psics.quantity.units.Units;

@ModelType(info = "Rastser plot for multiple runs and/or multiple recorders. Instead of " +
		"plotting voltage against time, the raster shows the voltage color coded along a single " +
		"line for each run or each recorder. Multiple runs are shown in a single block one under " +
		" the other. If there are multiple recorders, then each gives a separate block ",
		 standalone = false, tag = "Spike raster plot", usedWithin = { ViewConfig.class })
public class Raster {

		@Identifier(tag = "ID for the raster - used for the imae name")
		public String id;

		@IntegerNumber(range = "(100, 800)", required = false, tag = "default width for plots")
		public NDNumber width = new NDNumber(400);

		@IntegerNumber(range = "(100, 600)", required = false, tag = "default height for plots")
		public NDNumber height = new NDNumber(400);


		@ReferenceToFile(fallback = "", required = true, tag = "The name of the file containing the data")
		public String file;

		@Label(info = "Color table to use for the membrane potential. As yet, the only option " +
				"is 'grey'", tag = "Membrane potential color table")
		public String colors;

		@Quantity(range="(-100, 100)", required=false, tag="lower cutoff for potential", units=Units.none)
		public NDValue vmin = new NDValue(-80.);

		@Quantity(range="(-100, 100)", required=false, tag="upper cutoff for potential", units=Units.none)
		public NDValue vmax = new NDValue(-80.);


		@Quantity(range="(-100, 100)", required=false, tag="threshold for event marking", units=Units.none)
		public NDValue threshold = new NDValue(-100.);


		public String getID() {
			if (id == null) {
				id = "raster0";
			}
			return id;
		}


		public void setID(String s) {
			id = s;
		}

		public int getWidth() {
			return width.getValue();
		}

		public int getHeight() {
			return height.getValue();
		}


		public String getFileName() {
			return file;
		}


		public double getVMax() {
			 return vmax.getValue();
		}


		public double getVMin() {
			 return vmin.getValue();
		}


		public double getThreshold() {
			return threshold.getValue();
		}


}

