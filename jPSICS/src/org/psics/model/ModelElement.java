package org.psics.model;

import java.util.ArrayList;
import java.util.HashMap;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.ElementAdder;
import org.psics.be.Parameterized;
import org.psics.be.TextForm;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.model.control.About;

public class ModelElement implements ElementAdder, Parameterized {
	
	@TextForm(pos=-1, label="", ignore="")
	@Identifier(tag="Identifier (name) for the element; unique within the model")
	public String id;

	public String info;

   @Container(contentTypes = {About.class}, tag = "Extended textual information about the model")
   public ArrayList<About> c_abouts = new ArrayList<About>();

   @Container(contentTypes = {Parameter.class}, tag = "Parameters that can be used within the component")
   public ArrayList<Parameter> c_parameters = new ArrayList<Parameter>();
   
   HashMap<String, Parameter> p_paramhm = null;
   
   
   private HashMap<String, Double> p_phm;
   
   public void addElement(Object obj) {
	   if (obj instanceof About) {
		   if (c_abouts == null) {
			   c_abouts = new ArrayList<About>();
		   }
		   c_abouts.add((About)obj);
		   
	   } else if (obj instanceof Parameter) {
		   if (c_parameters == null) {
			   c_parameters = new ArrayList<Parameter>();
		   }
		   c_parameters.add((Parameter)obj);
		   p_phm = null;
	   } else if (this instanceof AddableTo) {
		   ((AddableTo)this).add(obj);
	   
	   } else {
		   E.error("cant add " + obj + " to " + this);
	   }
   }


   public HashMap<String, Double> getVariables() {
	   if (p_phm == null) {
		   p_phm = new HashMap<String, Double>();
		   if (c_parameters != null) {
			   for (Parameter p : c_parameters) {
				   p_phm.put(p.getName(), new Double(p.getDoubleValue()));
			   }
		   }
	   }
	   return p_phm;
   }
  
   public boolean hasParameter(String s) {
	   if (p_paramhm == null) {
		   makeParamHM();
	   }
	   boolean ret = false;
	   if (p_paramhm.containsKey(s)) {
		   ret = true;
	   }
	   return ret;
   }
   
   private void makeParamHM() {
	   p_paramhm = new HashMap<String, Parameter>();
	   for (Parameter p : c_parameters) {
		   p_paramhm.put(p.getName(), p);
	   }
   }


   public Parameter getParameter(String pnm) {
	  if (p_paramhm == null) {
		   makeParamHM();
	   }
	  return p_paramhm.get(pnm);
	}
}