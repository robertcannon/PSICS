package org.catacomb.serial.xml;

import org.catacomb.be.ReReferencable;
import org.catacomb.interlish.report.PrintProgressReport;
import org.catacomb.interlish.structure.*;
import org.catacomb.report.E;


import java.util.ArrayList;
import java.util.StringTokenizer;


public class XMLReader {

   ProgressReport progrept;
   Constructor instantiator;
   int sourceLength;
   int nerror;

   double progressFraction;

   String startText;

   public XMLReader(Constructor insta) {
      instantiator = insta;
   }


   public void setProgressReport(ProgressReport pr) {
      progrept = pr;
   }


   public void err(String s) {
      System.out.println(s);
   }


   public Object readObject(String s) {
      return readFromString(s);
   }


   public Object read(String s) {
      return readFromString(s);
   }



   public Object readFromString(String sin) {
	   String s = sin;
      s = XMLChecker.deGarbage(s);

      if (s == null) {
         return null;
      }

      startText = s.substring(0, Math.min(200, s.length()));

      progressFraction = 0.;

      nerror = 0;
      sourceLength = (new StringTokenizer(s, "\n")).countTokens();


      if (progrept != null) {
         progrept.setFraction(0.);
      } else if (sourceLength > 10000) {
         progrept = new PrintProgressReport();
         progrept.setFraction(0.);
      }

      XMLTokenizer tkz = new XMLTokenizer(s);

      XMLToken xmlt = tkz.nextToken();
      while (xmlt.isIntro() || xmlt.isComment()) {
         xmlt = tkz.nextToken();
      }



      XMLHolder xmlHolder = new XMLHolder();

      readFieldIntoParent(tkz, xmlHolder, xmlt);

      return xmlHolder.getContent();
   }





   public XMLToken readToken(XMLTokenizer tkz) {
      XMLToken xmlt = tkz.nextToken();

      int lno = tkz.lineno();
      if (nerror > 4) {
         err("too many errors - aborting parse at line " + lno);
         xmlt.setType(XMLToken.NONE);
         if (progrept != null) {
            progrept.setText("aborted at line " + lno);
         }
      }

      if (progrept != null) {
         double ff = lno / (sourceLength + 1.);
         if (ff > progressFraction + 0.005) {
            progrept.setFraction(ff);
            progressFraction = ff;
         }
         // progrept.setProgressText("line " + lno);
      }
      return xmlt;
   }



   public void readFieldIntoParent(XMLTokenizer tkz, Object parent, XMLToken start) {



      // read the child object that is known to the parent as item.name
      // if the parent is a vector, the object is added as a new element;
      // if the parent is a string, the xml is just apended;
      // otherwise the field is set.


      if (!start.isOpen()) {
         nerror++;
         err("ERROR - read object start item was not an open tag " + start);
         return;
      }


      Object child = null;

      if (parent instanceof String || parent instanceof StringBuffer) {
         child = new StringBuffer();
         ((StringBuffer)child).append(start.getOpenTagString());

      } else {

         // attributes may contain the class - the instantiator processes
         // all the attributes here
         Attribute[] atts = start.getAttributes();
         child = instantiator.getChildObject(parent, start.getName(), atts);
         if (child != null) {
            instantiator.applyAttributes(child, atts);
         }


         if (child == null) {
            child = new ArrayList();

         } else if (child instanceof String) {
            // in this case, set its length to 0. Subseqnet parts of the
            // string will get appended to the current value, so want to
            // keep track of the fact that it is a string, without keeping
            // the default that may have come from above;
            child = new StringBuffer();


         } else if (child.getClass().isArray()) {
            // make it an array list for the time being, then
            // give the lsit to teh instantiator to make into theright sort of
            // array;
            child = new ArrayList();
         }


         if (start.isClose()) {
            // the tag was both an open and a close tag, so now that we've
            // processed the attributes, we're done;


         } else {
            // read on and fill in fields until we get a closing tag which
            // matches the start tag
            // the fields will be inserted in target;

            XMLToken next = readToken(tkz);

            while (true) {
               if (next.isNone()) {
                  // should mean EOF, but could also be an error
                  // return whatever;
                  break;


               } else if (next.isOpen()) {
                  // open tags could mean anything - elementary field, array,
                  // or object, but in any case, pass them back to this method;
                  readFieldIntoParent(tkz, child, next);


               } else if (next.isClose()) {
                  if (next.closes(start)) {
                     // fine - close item

                     if (parent instanceof String || parent instanceof StringBuffer) {
                        ((StringBuffer)child).append(next.getCloseTagString());
                     }


                  } else {
                     nerror++;
                     E.shortError(" non-matching close item \n" + "start Item was: \n"
                           + start.toString() + "\n" + "but close was: \n" + next.toString() + "\n" +
                           "start text was \n" + startText);
                  }

                  // stop anyway - either its the right close item, or
                  // the wrong one but lets cary on and see what happens;
                  break;


               } else if (next.isString()) {
                  // this occurs if we're just reading a simple string
                  // field into the parent, or if we're in an array of strings;
                  // first case obj is defined, so reset it;
                  // second case put it in the vector;


                  if (child instanceof ArrayList) {
                     E.error("attempted to read string into array list?  - ignored" + next.svalue);

                     // ((ArrayList)child).add(next.svalue);


                  } else if (child instanceof StringBuffer) {
                     E.deprecate("xml reader - string added to string buffer " + next.svalue);

                     StringBuffer sbo = (StringBuffer)child;
                     String ssf = sbo.toString();
                     if (ssf.endsWith(">") || next.svalue.startsWith("<") || ssf.length() == 0) {
                        sbo.append(next.svalue);

                     } else {
                        sbo.append(" ");
                        sbo.append(next.svalue);
                     }


                  } else {
                     if (child instanceof String && ((String)child).length() > 0) {
                        child = child + " " + next.svalue;
                        E.deprecate("appended string to string " + next.svalue);

                        // april 06 - is this needed anywhere? reads floating attribues
                        // when written as elements (?)
                     }  else if (child instanceof Double && ((Double)child).doubleValue() == 0.0) {
                        child = new Double(next.svalue);

                     } else if (child == null) {
                        child = next.svalue;

                     } else {
                        instantiator.appendContent(child, next.svalue);

                     }
                  }


               } else if (next.isNumber()) {
                  E.shortError("XMLReader sjhould never return numbers....!!!! but " + "just got " + next);
               }
               next = readToken(tkz);
            }
         }

         // presumably got a close object, and have done one of:
         // a) filled the parameters of obj;
         // b) replaced obj with a new object of the same type
         // c) filled the vector with strings, doubles or objects;



         if (child instanceof StringBuffer) {
            child = ((StringBuffer)child).toString();
         }


         if (child instanceof ReReferencable) {
            ((ReReferencable)child).reReference();
         }


         if (parent instanceof StringBuffer) {
            StringBuffer psb = (StringBuffer)parent;
            psb.append(child);
            psb.append("\n");


         } else if (parent instanceof XMLHolder) {
            ((XMLHolder)parent).setContent(child);

         } else if (parent instanceof ArrayList) {
            E.missing("Array list parent in xmlreader is ignored! - child discarded " + child
                  + " parent=" + parent + " " + start.getName());
            // ((ArrayList)parent).add(child);
            setListChild(parent, child);

         } else {
            instantiator.setField(parent, start.getName(), child);
         }
      }
   }



   @SuppressWarnings({"unchecked"})
    private void setListChild(Object parent, Object child) {
      ((ArrayList)parent).add(child);
   }
}




class XMLHolder {

   Object content;


   public void setContent(Object obj) {
      content = obj;
   }


   public Object getContent() {
      return content;
   }
}


