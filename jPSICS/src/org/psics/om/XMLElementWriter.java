package org.psics.om;

import java.util.Collection;

import org.psics.be.Attribute;
import org.psics.be.Element;



public class XMLElementWriter {



   public static void appendElement(StringBuffer sbv, String psk, Element elt) {

	   boolean oneLine = elt.singleLine();

      Collection<Attribute> atts = elt.getAttributes();
      Collection<Element> elts = elt.getElements();
      String stxt = elt.getText();

      boolean hasElts = false;
      if (elts != null && elts.size() > 0) {
	      hasElts = true;
      }

      boolean hasBody = false;
      if (stxt != null && stxt.length() > 0) {
	      hasBody = true;
      }


      sbv.append(psk);
      sbv.append("<");
      sbv.append(elt.getName());
      int nchar = psk.length() + elt.getName().length() + 2;

      if (atts != null) {
			boolean first = true;
			for (Attribute att : atts) {
				String val = att.getValue();
				if (val != null) {
					int alen = att.getName().length() + att.getValue().length() + 3;


				if (first || oneLine || nchar + alen < 80) {
					sbv.append(" ");
					nchar += alen;
				} else {
					sbv.append("\n");
					sbv.append(psk);
					sbv.append("    ");
					nchar = psk.length() + 6;
				}
				sbv.append(att.getName());
				sbv.append("=\"");
				sbv.append(att.getValue());
				sbv.append("\"");
				first = false;
				}
			}
		}


      if (hasElts || hasBody) {
			sbv.append(">");

			if (hasElts) {
				sbv.append("\n");
				for (Element subelt : elts) {
					appendElement(sbv, psk + "   ", subelt);
				}
				sbv.append(psk);
			}

			if (hasBody) {
				sbv.append(stxt);
			}
			sbv.append("</");
			sbv.append(elt.getName());
			sbv.append(">\n");
			sbv.append("\n");

		} else {
			sbv.append("/>\n");
		}
	}
}
