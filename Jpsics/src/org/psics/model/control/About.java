package org.psics.model.control;


import org.psics.be.BodyValued;
import org.psics.model.channel.KSChannel;
import org.psics.model.control.PSICSRun;
import org.psics.quantity.annotation.ModelType;

@ModelType(info = "An About block can be used to contain a paragraph of text asociated with a model. A model " +
		"can include any number of About blocks.", standalone = false,
		tag = "Textual infomation about a model", usedWithin = { PSICSRun.class, KSChannel.class })
public class About implements BodyValued {

	public StringBuffer sbtext = new StringBuffer();

	// TODO should get the whole thing!
	public void setBodyValue(String s) {
		 sbtext.append(" ");
		 sbtext.append(s);
	}

	public String getText() {
		return sbtext.toString();
	}

}
