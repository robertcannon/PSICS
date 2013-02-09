package org.psics.write;

import org.psics.be.TextForm;
import org.psics.quantity.phys.IntegerQuantity;
import org.psics.quantity.phys.PhysicalQuantity;

public class ValueField extends WritableField {

	Object value;
	TextForm ann;

	public ValueField(String s, double p, Object v, TextForm tf) {
		super(s, p);
		value = v;
		ann = tf;

	}



	public String write(ListPosition lp) {
		StringBuffer sb = new StringBuffer();

		String txt = ann.label();

		if (value instanceof PhysicalQuantity) {
			PhysicalQuantity pq = (PhysicalQuantity)value;
			String sv = pq.getOriginalText();

				sb.append(intext(ann.ignore(), txt, sv, lp));


		} else if (value instanceof IntegerQuantity) {
				IntegerQuantity pq = (IntegerQuantity)value;
				String sv = pq.getOriginalText();

					sb.append(intext(ann.ignore(), txt, sv, lp));

		} else if (value instanceof String) {
			String sv = (String)value;

				sb.append(intext(ann.ignore(), txt, sv, lp));

		} else {
			sb.append(" todo - " + value.getClass().getName());
		}


		return sb.toString();
	}


	private String intext(String ignore, String txt, String val, ListPosition lp) {
		String ret = "";
		if (txt != null && val != null && !txt.equals(ignore)) {
		if (txt.indexOf("$") >= 0) {
			ret = " " + txt.replace("$", val);

		} else {
			ret = " has " + txt + " " + val + ",\n";
		}
		}
		if (ret != null && ret.length() > 0) {
			if (lp == ListPosition.SOLE) {
				// ret = capitalize(ret) + ". ";
				ret = ret + ".";
			} else if (lp == ListPosition.FIRST) {
				// ret = capitalize(ret);

			} else if (lp == ListPosition.INNER) {
				ret = ", " + ret;
			} else if (lp == ListPosition.LAST) {
				ret = "and " + ret + ".";
			}
		}
		return ret;
	}
}
