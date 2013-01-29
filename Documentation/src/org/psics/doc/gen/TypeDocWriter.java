package org.psics.doc.gen;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.psics.be.ContainerForm;
import org.psics.be.E;
import org.psics.be.Element;
import org.psics.be.Exampled;
import org.psics.be.TextForm;
import org.psics.om.OmBuilder;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Expression;
import org.psics.quantity.annotation.Flag;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.FolderPath;
import org.psics.quantity.annotation.LibraryPath;
import org.psics.quantity.annotation.Metadata;
import org.psics.quantity.annotation.ModelType;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.Quantities;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.ReferenceByLabel;
import org.psics.quantity.annotation.ReferenceToFile;
import org.psics.quantity.annotation.StringEnum;
import org.psics.quantity.annotation.SubComponent;
import org.psics.quantity.phys.PhysicalQuantity;
import org.psics.quantity.units.Units;
import org.psics.util.ClassUtil;


public class TypeDocWriter {


	Object srcObj;

	String eltName;
	ArrayList<String> atts;


	public TypeDocWriter(Object obj) {
		srcObj = obj;
		atts = new ArrayList<String>();
	}


	public String getExampleXML() {
		// String nm = ClassUtil.getUnqualifiedName(srcObj);
		return null;
	}


	public String getElementName() {
		return eltName;
	}

	public ArrayList<String> getAttributes() {
		return atts;
	}

	  @SuppressWarnings("unchecked")
	public Element getDocElement() {
	 	OmBuilder omb = new OmBuilder();

		Element elt = omb.newElement("TypeDoc");
		String nm = ClassUtil.getUnqualifiedName(srcObj);
		omb.addAttribute(elt, "type", nm);
		eltName = nm;

		Class<?> srcClass = null;
		if (srcObj instanceof Class) {
			srcClass = (Class)srcObj;
		} else {
			srcClass = srcObj.getClass();
		}
		Annotation[] caa = srcClass.getAnnotations();
		if (caa != null) {
			for (Annotation ant : caa) {
				if (ant instanceof ModelType) {
					ModelType mt = (ModelType)ant;
					omb.addAttribute(elt, "tag", mt.tag());
					if (mt.standalone()) {
						omb.addAttribute(elt, "standalone", "true");
					}
					if (mt.usedWithin() != null && mt.usedWithin().length > 0) {
						Element uelt = omb.newElement("UsableIn");
						omb.addElement(elt, uelt);

						for (Class<?> c : mt.usedWithin()) {
							Element sub = omb.newElement("PossibleParent");
							omb.addElement(uelt, sub);
							omb.addAttribute(sub, "type", ClassUtil.getUnqualifiedName(c));
						}
					}

					Element felt = omb.newElement("Info");
    				omb.addElement(elt, felt);
    				omb.setBody(felt, mt.info());

				} else {
					E.error("unhandled classs annotation");
				}
			}

		}


		for (Field fld : srcClass.getFields()) {
			Annotation[] aa = fld.getAnnotations();

		    if (aa != null && aa.length > 0) {
		    	boolean inin = false;
		    	for (Annotation ant : aa) {
		     		if (ant instanceof Quantity) {

		    			if (fld.getType().equals(Double.TYPE) ||
		    					PhysicalQuantity.class.isAssignableFrom(fld.getType())) {
		    				inin = true;
		    				Element felt = omb.newElement("DoubleField");
		    				omb.addElement(elt, felt);
		    				omb.addAttribute(felt, "name", fld.getName());
		    				Quantity qa = (Quantity)ant;
		    				omb.addAttribute(felt, "range", qa.range());
		    				omb.addAttribute(felt, "tag", qa.tag());
		    				Units us = qa.units();
		    				if (us == null) {
		    					omb.addAttribute(felt, "units", "none");
		    				} else {
		    					omb.addAttribute(felt, "units", us.name()); // TODO more info from units?
		    				}
		    				if (qa.required()) {
		    					omb.addAttribute(felt, "required", "true");
		    				}

		    			} else {
		    				E.warning("unhandled field with quantity annotation " + fld);
		    			}



		     		} else if (ant instanceof Quantities) {
		     			inin = true;
		     			Element felt = omb.newElement("DoubleArray");
	    				omb.addElement(elt, felt);
	    				omb.addAttribute(felt, "name", fld.getName());
	    				Quantities qa = (Quantities)ant;
	    				omb.addAttribute(felt, "range", qa.range());
	    				omb.addAttribute(felt, "tag", qa.tag());
	    				omb.addAttribute(felt, "info", qa.info());
	    				Units us = qa.units();
	    				if (us == null) {
	    					omb.addAttribute(felt, "units", "none");
	    				} else {
	    					omb.addAttribute(felt, "units", us.name()); // TODO more info from units?
	    				}
	    				if (qa.required()) {
	    					omb.addAttribute(felt, "required", "true");
	    				}




		     		} else if (ant instanceof IntegerNumber) {
		     			inin = true;
			    				Element felt = omb.newElement("NumberField");
			    				omb.addElement(elt, felt);
			    				omb.addAttribute(felt, "name", fld.getName());
			    				IntegerNumber qa = (IntegerNumber)ant;
			    				omb.addAttribute(felt, "range", qa.range());
			    				omb.addAttribute(felt, "tag", qa.tag());

			    				if (qa.required()) {
			    					omb.addAttribute(felt, "required", "true");
			    				}




		     		} else if (ant instanceof Flag) {
		     			inin = true;
			    			if (fld.getType().equals(Boolean.TYPE)) {

			    				Element felt = omb.newElement("FlagField");
			    				omb.addElement(elt, felt);
			    				omb.addAttribute(felt, "name", fld.getName());
			    				Flag fa = (Flag)ant;
			    				omb.addAttribute(felt, "tag", fa.tag());
			    				if (fa.required()) {
			    					omb.addAttribute(felt, "required", "true");
			    				}

			    			} else {
			    				E.error("unhandled field with Flag annotation " + fld);
			    			}


		    		} else if (ant instanceof Identifier) {
		    			if (fld.getType().equals(String.class)) {
		    				inin = true;
		    				Element felt = omb.newElement("Identifier");
		    				omb.addElement(elt, felt);
		    				omb.addAttribute(felt, "name", fld.getName());
		    				omb.addAttribute(felt, "tag", ((Identifier)ant).tag());

		    				omb.addAttribute(felt, "required", "true");

		    			} else {
		    				E.error("non-string identifier? " + fld);
		    			}

		    		} else if (ant instanceof Container) {
		    			if (fld.getType().equals(ArrayList.class)) {
		    			//	inin = true;
		    				Container ctr = (Container)ant;
		    				Element felt = omb.newElement("Container");
		    				omb.addElement(elt, felt);
		    				omb.addAttribute(felt, "name", fld.getName());
		    				omb.addAttribute(felt, "tag", ctr.tag());

		    				for (Class<?> c : ctr.contentTypes()) {
		    					omb.addBodyElement(felt, "ContentType", c.getSimpleName());
		    				}

		    			} else {
		    				E.error("non-list container? " + fld);
		    			}

		    		} else if (ant instanceof SubComponent) {
		    				inin = true;
		    			atts.add(fld.getName());
		    				SubComponent ctr = (SubComponent)ant;
		    				Element felt = omb.newElement("SubComponent");
		    				omb.addElement(elt, felt);
		    				omb.addAttribute(felt, "name", fld.getName());
		    				omb.addAttribute(felt, "tag", ctr.tag());
		    				omb.addBodyElement(felt, "ContentType", ctr.contentType().getSimpleName());


		    		} else if (ant instanceof ReferenceByIdentifier) {
		    			if (fld.getType().equals(String.class)) {
		    				inin = true;
		    				ReferenceByIdentifier rbi = (ReferenceByIdentifier)ant;
		    				Element felt = omb.newElement("ReferenceByIdentifier");
		    				omb.addElement(elt, felt);
		    				omb.addAttribute(felt, "name", fld.getName());
		    				omb.addAttribute(felt, "tag", rbi.tag());
		    				if (rbi.required()) {
		    					omb.addAttribute(felt, "required", "true");
		    				}
		    				for (Class<?> c : rbi.targetTypes()) {
		    					omb.addBodyElement(felt, "TargetType", c.getSimpleName());
		    				}
		    			} else {
		    				E.error("non-string identifier? " + fld);
		    			}


		    		} else if (ant instanceof ReferenceToFile) {
		    			inin = true;
		    			ReferenceToFile rtf = (ReferenceToFile)ant;
		    			Element felt = omb.newElement("ReferenceToFile");
	    				omb.addElement(elt, felt);
	    				omb.addAttribute(felt, "name", fld.getName());
	    				omb.addAttribute(felt, "tag", rtf.tag());
	    				if (rtf.required()) {
	    					omb.addAttribute(felt, "required", "true");
	    				}
	    				omb.addAttribute(felt, "default", rtf.fallback());

		    		} else if (ant instanceof LibraryPath) {
		    			LibraryPath rtf = (LibraryPath)ant;
		    			Element felt = omb.newElement("LibraryPath");
	    				omb.addElement(elt, felt);
	    				omb.addAttribute(felt, "name", fld.getName());
	    				omb.addAttribute(felt, "tag", rtf.tag());
	    				if (rtf.required()) {
	    					omb.addAttribute(felt, "required", "true");
	    				}
	    				omb.addAttribute(felt, "default", rtf.fallback());

		    		} else if (ant instanceof FolderPath) {
		    			FolderPath rtf = (FolderPath)ant;
		    			Element felt = omb.newElement("FolderPath");
	    				omb.addElement(elt, felt);
	    				omb.addAttribute(felt, "name", fld.getName());
	    				omb.addAttribute(felt, "tag", rtf.tag());
	    				if (rtf.required()) {
	    					omb.addAttribute(felt, "required", "true");
	    				}
	    				omb.addAttribute(felt, "default", rtf.fallback());


		    		} else if (ant instanceof ReferenceByLabel) {
		    			inin = true;
		    			ReferenceByLabel rbl = (ReferenceByLabel)ant;
		    			Element felt = omb.newElement("ReferenceByLabel");
	    				omb.addElement(elt, felt);
	    				omb.addAttribute(felt, "name", fld.getName());
	    				omb.addAttribute(felt, "tag", rbl.tag());
	    				if (rbl.required()) {
	    					omb.addAttribute(felt, "required", "true");
	    				}

		    		} else if (ant instanceof Expression) {

		    			Expression exp = (Expression)ant;
		    			Element felt = omb.newElement("Expression");
		    			omb.addElement(elt, felt);
		    			omb.addAttribute(felt, "name", fld.getName());
		    			omb.addAttribute(felt, "tag", exp.tag());

		    			if (exp.required()) {
		    				omb.addAttribute(felt, "required", "true");
		    			}



		    		} else if (ant instanceof StringEnum) {
		    			inin = true;
		    			StringEnum sen = (StringEnum)ant;
		    			Element felt = omb.newElement("Choice");
		    			omb.addElement(elt, felt);
		    			omb.addAttribute(felt, "name", fld.getName());
		    			omb.addAttribute(felt, "tag", sen.tag());
		    			if (sen.required()) {
		    				omb.addAttribute(felt, "required", "true");
		    			}
		    			StringTokenizer st = new StringTokenizer(sen.values(), ", ");
		    			while (st.hasMoreTokens()) {
		    				String s = st.nextToken();
		    				Element selt = omb.newElement("Possible");
		    				omb.addElement(felt, selt);
		    				omb.addAttribute(selt, "value", s);
		    			}


		    		} else if (ant instanceof Label) {
		    			inin = true;
		    			if (fld.getType().equals(String.class)) {

		    				Element felt = omb.newElement("Label");
		    				omb.addElement(elt, felt);
		    				omb.addAttribute(felt, "name", fld.getName());
		    				Label ha = (Label)ant;
		    				omb.addAttribute(felt, "tag", ha.tag());
		    				omb.addAttribute(felt, "info", ha.info());
		    			} else {
		    				E.error("unhandled field with Label annotation " + fld);
		    			}


		    		} else if (ant instanceof Metadata) {
		    			inin = true;
		    			if (fld.getType().equals(String.class)) {

		    				Element felt = omb.newElement("MetadataField");
		    				omb.addElement(elt, felt);
		    				omb.addAttribute(felt, "name", fld.getName());
		    				Metadata ha = (Metadata)ant;
		    				omb.addAttribute(felt, "tag", ha.tag());
		    				omb.addAttribute(felt, "info", ha.info());


		    			} else {
		    				inin = false;
		    				E.warning("unhandled field with Metadata annotation " + fld);
		    			}





		    		} else {
		    			if (ant instanceof TextForm || ant instanceof ContainerForm) {
		    				
		    			} else {
		    				E.warning("unrecognized annotation " + ant + " in " + srcClass);
		    			}
		    		}

		    	}

		    	if (inin) {
		    		if (fld.getName().startsWith("c_") || fld.getName().equals("id")) {
		    			// leave it out
		    		} else {
		    			atts.add(fld.getName());
		    		}
		    	}
			}
		}

		try {
			Object obj = srcClass.newInstance();
			if (obj instanceof Exampled) {
				String etxt = ((Exampled)obj).getExampleText();

				StringTokenizer st = new StringTokenizer(etxt, "|||");
				while (st.hasMoreTokens()) {
					Element exwrap = omb.newElement("ModelExample");
					omb.addElement(elt, exwrap);
					omb.setInnerXML(exwrap, st.nextToken());
				}

			}


		} catch (Exception e) {
			E.error("" + e);
		}

		return elt;
	}


}
