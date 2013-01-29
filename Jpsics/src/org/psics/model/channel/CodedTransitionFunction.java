package org.psics.model.channel;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.BodyValued;
import org.psics.be.E;
import org.psics.codgen.channel.CodedTransitionEvaluator;
import org.psics.model.Argument;
import org.psics.model.Constant;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Label;
import org.psics.quantity.annotation.ModelType;


@ModelType(standalone=false, usedWithin={KSChannel.class}, tag="Embedded functions for use with ad-hoc coded transition rates",
		info="This ")
public class CodedTransitionFunction implements AddableTo, BodyValued {

	@Label(info = "", tag = "The name of the function, as used from other code fragments in the channel definition.")
	public String name;

	@Label(info = "", tag = "Return type ('double', 'int', etc).")
	public String type;

	@Label(info = "", tag = "The name of the output variable returned from the function.")
	public String returnVariable;


	@Container(contentTypes = {Constant.class}, tag = "")
	public ArrayList<Constant> constants = new ArrayList<Constant>();

	@Container(contentTypes = {Argument.class}, tag = "")
	public ArrayList<Argument> arguments = new ArrayList<Argument>();



	private String codeFragment;


	public void add(Object obj) {
		if (obj instanceof Constant) {
			constants.add((Constant)obj);

		} else if (obj instanceof Argument) {
			arguments.add((Argument)obj);

		} else {
			E.error("cant add " + obj);
		}
	}



	public void setBodyValue(String s) {
		codeFragment = s;
	}


	private String[][] makeArgsArray() {
		String[][] ret = new String[arguments.size()][2];
		for (int i = 0; i < arguments.size(); i++) {
			Argument a = arguments.get(i);
			ret[i][0] = a.getType();
			ret[i][1] = a.getName();
		}
		return ret;
	}


	public void addTo(CodedTransitionEvaluator ret) {
		if (constants != null && constants.size() > 0) {
			E.missing("cant have constants in functions yet");
		}
		ret.addFunction(type, name, returnVariable, makeArgsArray(), codeFragment);

	}



	public CodedTransitionFunction deepCopy() {
		 CodedTransitionFunction ret = new CodedTransitionFunction();
		 ret.name = name;
		 ret.type = type;
		 ret.returnVariable = returnVariable;
		 ret.codeFragment = codeFragment;
		 for (Constant c : constants) {
			 ret.add(c.makeCopy());
		 }
		 for (Argument a : arguments) {
			 ret.add(a.makeCopy());
		 }
		 return ret;
	}




}
