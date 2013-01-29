package org.psics.codgen;


public class ExceptionReporter {




	   public static String getReadableCause(Exception ex) {
	   StringBuffer sb = new StringBuffer();
	   sb.append(ex.getClass().getName() + " " + ex.getMessage());

	   Throwable thr = ex.getCause();
	   while (thr != null) {
	      sb.append(" " + thr.getClass().getName() + " :  " + thr.getMessage() + "\n");
	      if (thr instanceof Exception) {
	         thr = ((Exception)thr).getCause();
	      } else {
	         thr = null;
	      }
	   }
	   return sb.toString();
	   }


}
