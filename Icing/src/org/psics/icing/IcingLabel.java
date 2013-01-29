package org.psics.icing;

import org.catacomb.be.Position;
import org.catacomb.graph.gui.Pickable;
import org.psics.be.E;
import org.psics.be.StringValued;


public class IcingLabel implements StringValued, Pickable {


	String text;
	IcingPoint target;

	int xpos;
	int ypos;

	int xtgt;
	int ytgt;

	// position of label rel to attachment point;
	int xrel = 8;
	int yrel = -8;

	boolean bvis;

	int w = 0;

	int icache = 0;

	public IcingLabel(String s, IcingPoint p) {
		text = s;
		target = p;
		bvis = true;
		xpos = -1;
		ypos = 0;
	}

	public void setText(String s) {
		text = s;
		w = 0;
	}


	public String toString() {
		return getStringValue();
	}


	public String getStringValue() {
		String ret = text;
		//if (target == null) {
		//	ret += " (unattached)";
		//}
		return ret;
	}



	public String getText() {
		return text;
	}

	public IcingPoint getTarget() {
		return target;
	}

	public void setTarget(IcingPoint p) {
		target = p;
	}

	public void setShow() {
		bvis = true;
	}

	public void setHide() {
		bvis = false;
	}

	public boolean visible() {
		return bvis;
	}


	public void saveLabelToTreePoint() {
		if (target != null) {
			target.setLabel(text);
			target.saveLabelToTreePoint();
		}
	}


	public int getCache() {
		return icache;
	}


	public Object getRef() {
		 return this;
	}


	public void setCache(int i) {
		icache = i;
	}


	public void setPosition(Position pos) {
		 E.info("trying to move label to " + pos);
	}


	public void setIntPosition(int ix, int iy) {
		xpos = ix;
		ypos = iy;
	}

	public void relativize() {
		xrel = xpos - xtgt;
		yrel = ypos - ytgt;
	}

	public int getXRel() {
		return xrel;
	}

	public int getYRel() {
		return yrel;
	}

	public void detach() {
		if (target != null) {
			target.setLabel(null);
			target.saveLabelToTreePoint();
		}
		target = null;
	}

	public boolean isFree() {
		boolean ret = false;
		if (target == null) {
			ret = true;
		}
		return ret;
	}
}
