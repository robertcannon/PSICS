package org.psics.quickxml;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class ElementExtractor {





   public static String getAttribute(String enm, String atnm, String src) {
      String ret = null;

      String etxt = getElementText(enm, src);

      if (etxt != null) {
	 ret = getAttribute(atnm, etxt);
      }

      return ret;
   }



   public static String getAttribute(String atnm, String src) {
      String ret = null;
      Pattern pat = Pattern.compile(atnm + "=\"(.*)\"");

      Matcher matcher = pat.matcher(src);
      if (matcher.find()) {
	 ret = matcher.group(1);
      }
      return ret;
   }





   public static String getElementText(String enm, String src) {

      String ret = getVerboseElementText(enm, src);

      if (ret == null) {
	 ret = getCompactElementText(enm, src);
      }
      return ret;
   }





   public static String getVerboseElementText(String enm, String src) {
      String ret = null;

      Pattern pat = Pattern.compile("<" + enm + ">(.*)</" + enm + ">");

      Matcher matcher = pat.matcher(src);
      if (matcher.find()) {
	 ret = matcher.group(1);
      }
      return ret;
   }




   public static String getCompactElementText(String enm, String src) {
      String ret = null;

      Pattern pat = Pattern.compile("<" + enm + " (.*)/>");

      Matcher matcher = pat.matcher(src);
      if (matcher.find()) {
	 ret = matcher.group(1);
      }
      return ret;
   }



public static ArrayList<String> getElementBodiesOfType(String enm, String txt) {
	 ArrayList<String> ret = new ArrayList<String>();
	 {
	 Pattern pat = Pattern.compile("<" + enm + " (.*)/>");
	 Matcher matcher = pat.matcher(txt);
	 while (matcher.find()) {
		 ret.add(matcher.group(1));
	 }
	 }

	 {
	 Pattern pat = Pattern.compile("<" + enm + ">(.*)</" + enm + ">");
	   Matcher matcher = pat.matcher(txt);
		 while (matcher.find()) {
			 ret.add(matcher.group(1));
		 }
	 }
	 return ret;
}



public static String[] getAttributes(String[] atts, String s) {   
		String[] ret = new String[atts.length];
		for (int i = 0; i < atts.length; i++) {
			Pattern pat = Pattern.compile(atts[i] + "=\"(.*?)\"");
			Matcher matcher = pat.matcher(s);
			if (matcher.find()) {
				ret[i] = matcher.group(1);
			} else {
				ret[i] = null;
			}
		}
		return ret;
}

public static String[][] getAttributeSets(String[] atts, String[] srcs) {   
	String[][] ret = new String[srcs.length][atts.length];

	Matcher[] mats = new Matcher[atts.length];
	for (int i = 0; i < atts.length; i++) {
		Pattern pat = Pattern.compile(atts[i] + "=\"(.*?)\"");
		mats[i] = pat.matcher("");
	}
	for (int isrc = 0; isrc < srcs.length; isrc++) {
		String s = srcs[isrc];
		for (int i = 0; i < mats.length; i++) {
			Matcher matcher = mats[i];
			matcher.reset(s);
			if (matcher.find()) {
				ret[isrc][i] = matcher.group(1);
			} else {
				ret[isrc][i] = null;
			}
		}
	}
	return ret;
}

}
