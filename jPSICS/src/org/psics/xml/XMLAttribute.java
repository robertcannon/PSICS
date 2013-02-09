package org.psics.xml;

import org.psics.be.Attribute;
 

public class XMLAttribute implements Attribute {

   String name;
   String value;


   public XMLAttribute(String s, String val) {
      name = s;
      value = val;
   }

   public String getName() { return name; }

   public String getValue() { return value; }

}
