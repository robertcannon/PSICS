package org.psics.be;


public interface Constructor {

   Object newInstance(String cnm);

   Object getChildObject(Object parent, String name, Attribute[] atta) throws BuildException;
   void applyAttributes(Object obj, Attribute[] atta, Parameterized ptzd);

   boolean setAttributeField(Object parent, String fieldName, String child, Parameterized ptzd);

   boolean setField(Object parent, String fieldName, Object child, Parameterized ptzd);

   Object getField(Object parent, String fieldName);

   void appendContent(Object child, String content);

   void setIntFromStatic(Object ret, String id, String sv);

   void addSearchPackage(Package pkg);

}
