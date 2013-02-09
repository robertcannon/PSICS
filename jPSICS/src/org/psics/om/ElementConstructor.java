
package org.psics.om;

import org.psics.be.Attribute;
import org.psics.be.Constructor;
import org.psics.be.E;
import org.psics.be.Parameterized;






public class ElementConstructor implements Constructor {


   public Object newInstance(String s) {
      return new OmElement(s);
   }

   public void addSearchPackage(Package pkg) {
	   E.warning("adding a search package is of no use in ElementConstructor");
   }


   public boolean setAttributeField(Object parent, String fieldName, String val, Parameterized ptzd) {
      if (parent instanceof OmElement) {
	 OmElement omp = (OmElement)parent;
	 omp.addAttribute(fieldName, val);
      }
      return true;
   }


   public void appendContent(Object obj, String s) {
      ((OmElement)obj).addToBody(s);
   }


   public Object getChildObject(Object parent, String name, Attribute[] atta) {

      OmElement elt = new OmElement(name);

      return elt;
   }


   public void applyAttributes(Object obj, Attribute[] atta, Parameterized ptzd) {
      ((OmElement)obj).copyAttributes(atta);
   }



   public boolean setField(Object parent, String fieldName, Object child, Parameterized ptzd) {
      boolean ok = false;

      if (parent instanceof OmElement && child instanceof String) {

	 OmElement omp = (OmElement)parent;
	 OmElement ec = new OmElement(fieldName);

	 ec.setBody((String)child);
	 omp.addElement(ec);


      } else if (parent instanceof OmElement && child instanceof OmElement) {
	 OmElement omp = (OmElement)parent;
	 OmElement omc = (OmElement)child;

	 if (omc.getName().equals(fieldName)) {
	    omp.addElement(omc);
	    ok = true;

	 } else {
	    E.error(" - element instantiator set field hs fieldname " +
			       fieldName + "  but element " + omc.getName());
	 }

      } else {
	 E.error(" - ElementInstantiator set field : fieldname=" + fieldName +
			    " parent=" + parent +
			   "    child=" + child + " " + child.getClass().getName() +
			    "  but need elements only");
	 (new Exception()).printStackTrace();
      }
      return ok;
   }


   public Object getField(Object parent, String fieldName) {
      return new OmElement(fieldName);
   }


   public void setIntFromStatic(Object ret, String id, String sv) {
      E.missing();
   }


}
