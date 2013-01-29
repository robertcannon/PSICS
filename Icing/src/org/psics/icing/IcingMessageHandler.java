package org.psics.icing;

import org.catacomb.druid.gui.base.DruDialog;
import org.catacomb.druid.gui.base.DruScrollingHTMLPanel;
import org.psics.be.MessageHandler;
import org.psics.be.MessageType;


public class IcingMessageHandler implements MessageHandler {

	DruDialog dialog;
	DruScrollingHTMLPanel panel;

	StringBuffer sbwk = new StringBuffer();


	public IcingMessageHandler(DruDialog d, DruScrollingHTMLPanel p) {
		dialog = d;
		panel = p;
	}


	public void show() {
		dialog.open();
	}

	public void clearMessages() {
		sbwk = new StringBuffer();
		showSB();
	}

	public void msg(MessageType type, String txt) {
		String psty = "";
		if (type.equals(MessageType.ERROR)) {
			psty = "color : #f00000;";
		}

		if (psty.length() > 0) {
			sbwk.append("<p style=\"" + psty + "\">");
		} else {
			sbwk.append("<p>");
		}

			sbwk.append(type.name());
			sbwk.append(" - ");
			sbwk.append(txt);


		sbwk.append("</p>\n");
		showSB();
		if (type.equals(MessageType.ERROR)) {
			show();
		}

	}


	public void msg(String txt) {
		sbwk.append(txt);
		sbwk.append("<br>\n");
		showSB();
	}

	private void showSB() {
		StringBuffer hsb = new StringBuffer();
		hsb.append("<html><head></head><body>\n");
		hsb.append(sbwk.toString());
		hsb.append("</body></html>\n");

		panel.setText(hsb.toString());
	}

}
