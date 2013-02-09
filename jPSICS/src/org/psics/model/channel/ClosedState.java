package org.psics.model.channel;

import org.psics.quantity.annotation.ModelType;



@ModelType(tag="non-permeable configuration of an ion channel", info="", standalone=false,
		usedWithin={KSChannel.class})
public class ClosedState extends KSState {


	public ClosedState() {
	}


	public ClosedState(String s) {
		super(s);
	}

	public double getRelativeConductance() {
		return 0;
	}

	public String getExampleText() {
		 return "<ClosedState id=\"C\"/>";
	}


	public KSState deepCopy() {
		ClosedState ret = new ClosedState();
		super.copyInto(ret);
		return ret;
	}
}
