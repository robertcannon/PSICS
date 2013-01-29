package org.psics.model.display;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.SubComponent;
import org.psics.quantity.phys.NDNumber;


public abstract class BaseGraph implements AddableTo {

	@SubComponent(contentType = XAxis.class, tag = "default x axis range and label")
	public XAxis xaxis;

	@SubComponent(contentType = YAxis.class, tag = "default y axis range and label")
	public YAxis yaxis;

	@Container(contentTypes = { View.class }, tag = "Views of the data")
	public ArrayList<View> c_views = new ArrayList<View>();

	@IntegerNumber(range = "(100, 800)", required = false, tag = "default width for plots")
	public NDNumber width = new NDNumber(400);

	@IntegerNumber(range = "(100, 600)", required = false, tag = "default height for plots")
	public NDNumber height = new NDNumber(400);


	public abstract void add(Object obj);

	public ArrayList<View> getViews() {
		 return c_views;
	}

	public Axis getXAxis() {
		if (xaxis == null) {
			xaxis = new XAxis();
			xaxis.label = "X";
		}
		return xaxis;
	}

	public Axis getYAxis() {
	  if (yaxis == null) {
		yaxis = new YAxis();
		yaxis.label = "Y";
	    }
		return yaxis;
	}



	public int getWidth() {
		return width.getNativeValue();
	}

	public int getHeight() {
		return height.getNativeValue();
	}


	public void addItem(Object obj) {

	if (obj instanceof View) {
		c_views.add((View)obj);
	} else if (obj instanceof XAxis) {
		xaxis = (XAxis)obj;
	} else if (obj instanceof YAxis) {
		yaxis = (YAxis)obj;
	} else {
		E.error("cant add " + obj);
	}
	}

}
