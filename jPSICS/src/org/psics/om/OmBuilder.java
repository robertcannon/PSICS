package org.psics.om;

import org.psics.be.Element;
import org.psics.be.ElementFactory;
import org.psics.be.Specified;
import org.psics.be.Specifier;


public class OmBuilder implements ElementFactory {



   public OmBuilder() {

   }

   // for elements that should ocntain all their context as attibutes (ie, the package)
   public Element makeStandaloneElementFor(Object obj) {
      Element elt = null;

      if (obj instanceof Specified) {
	 elt = makeSpecifiedElement((Specified)obj);

      } else {
	 elt = makeClassElement(obj);
      }

      return elt;
   }



   private Element makeSpecifiedElement(Specified spd) {
      Specifier sp = spd.getSpecifier();

      String sch = sp.getSpecifiedTypeName(spd);

      OmElement ome = new OmElement();
      ome.setName(sch);
      return ome;
   }


   private Element makeClassElement(Object obj) {
      String cnm = obj.getClass().getName();
      int ild = cnm.lastIndexOf(".");
      String pkg = "";
      String enm = cnm;
      if (ild >= 0) {
	 enm = cnm.substring(ild+1, cnm.length());
	 pkg = cnm.substring(0, ild);
      }
      OmElement elt = new OmElement();
      elt.setName(enm);
      elt.addAttribute("package", pkg);
      return elt;
   }



   // for elements within a known context - just get the class name and use it for the name
   public Element makeElementFor(Object obj) {
      String  cnm = obj.getClass().getName();
      int ild = cnm.lastIndexOf(".");
      if (ild >= 0) {
	 cnm = cnm.substring(ild+1, cnm.length());
      }
      OmElement elt = new OmElement();
      elt.setName(cnm);
      return elt;
   }



   public Element newElement(String nm) {
	   return makeElement(nm);
   }


   public Element makeElement(String name) {
      OmElement elt = new OmElement();
      elt.setName(name);
      return elt;
   }

   public Element makeElement(String name, String body) {
      OmElement elt = new OmElement();
      elt.setName(name);
      elt.setBody(body);
      return elt;
   }

   public void setOneLine(Element elt) {
	   ((OmElement)elt).setOneLine();
   }



   // in following the objects are whatever class is returned by the above two;
   public void addAttribute(Element elt, String name, String value) {
      ((OmElement)elt).addAttribute(name, value);
   }


   public void addAttribute(Element elt, String name, int value) {
	      ((OmElement)elt).addAttribute(name, "" + value);
   }

   public void addAttribute(Element elt, String name, double value, String fmt) {
      ((OmElement)elt).addAttribute(name, String.format(fmt, value));
   }


   public void addElement(Element parent, Object child) {
      ((OmElement)parent).addElement((OmElement)child);
   }

   protected String formatDouble(double d) {
	      String ret = "0";
	      if (d != 0.0) {
	         ret = String.format("%8.3g", new Double(d)).trim();
	      }
	      return ret;
	   }



	   public void addBodyElement(Element e, String sn, String sv) {
		   OmElement ome = (OmElement)e;
	      if (sv != null && sv.length() > 0) {
	         OmElement elt = new OmElement(sn);
	         elt.setBody(sv);
	         ome.addElement(elt);
	      }
	   }



	   public void addAttribute(OmElement ome, String sn, double d) {
	     addAttribute(ome, sn, formatDouble(d));
	   }

	public void setBody(Element felt, String s) {
			((OmElement)felt).setBody(s);
	}

	public void setInnerXML(Element elt, String etxt) {
		 ((OmElement)elt).setInnerXML(etxt);
	}

}

