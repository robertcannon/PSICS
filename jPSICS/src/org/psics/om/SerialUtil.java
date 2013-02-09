package org.psics.om;

import org.psics.be.E;



public class SerialUtil {


   public static String serializeDoubleArray(double[] d) {
      return stringify(d);
   }


   public static Object stringifyObject(Object val, String dblFormat) {
      Object value = null;

      if (val == null) {
         value = null;

      } else if (val instanceof Character) {
         E.error("found char data in wrong place " + val);
         value = ((Character)val).toString();

      } else if (val instanceof String) {
         value = val;

      } else if (val instanceof Integer) {
         value = val.toString();

      } else if (val instanceof Double) {

    	  value = String.format(dblFormat, val);

    	 // E.info("serialized " + val + " to " + value);

      } else if (val instanceof Boolean) {
         value = val.toString();

      } else if (val instanceof Long) {
         value = val.toString();

      } else if (val instanceof double[]) {
         value = stringify((double[])val, dblFormat);

         E.info("serialized da " + value);

      } else if (val instanceof int[]) {
         value = stringify((int[])val);

      } else {
         value = val;
      }
      return value;
   }

   final static String stringify(double[] da) {
	   return stringify(da, null);
   }

   final static String stringify(double[] da, String format) {
      StringBuffer sb = new StringBuffer();
      String sform = format;
      if (sform == null) {
    	  sform = "%.4g";
      }

      int nda = da.length;
      for (int i = 0; i < nda; i++) {
            sb.append(String.format(sform, new Double(da[i])));
//         sb.append(Double.toString(da[i]));
         if (i < nda - 1) {
            sb.append(", ");
         }
      }
      return sb.toString();
   }


   public static String format(double d, String format) {
	   String ret = null;
	    if (format != null) {
	    	ret = String.format(format, new Double(d));
	    } else {
	    	ret = String.format("%.4g", new Double(d));
	    }
	    return ret;
   }


   final static String stringify(int[] da) {
      StringBuffer sb = new StringBuffer();

      int nda = da.length;
      for (int i = 0; i < nda; i++) {
         sb.append(Integer.toString(da[i]));
         if (i < nda - 1) {
            sb.append(", ");
         }
      }
      return sb.toString();
   }

}
