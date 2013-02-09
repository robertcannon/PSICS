package org.psics;

import org.psics.be.E;
import org.psics.samples.math.MathEx;
import org.psics.model.math.Function;
import org.psics.util.JUtil;
import org.psics.xml.ReflectionInstantiator;
import org.psics.xml.XMLReader;

public class FormatTest {

	
	public static void main(String[] argv) {
		FormatTest ft = new FormatTest();
		ft.readMath();
	}
	
	public void readMath() {
		String s = JUtil.getRelativeResource(MathEx.class, "rf1.xml");
		ReflectionInstantiator rin = new ReflectionInstantiator();
		rin.addSearchPackage("org.psics.model.math");
		XMLReader xmlr = new XMLReader(rin);
		try { 
		    Object obj = xmlr.read(s);
		    E.info("read " + obj);
		    Function f = (Function)obj;
		    double[] args = {1., 2., 3., 4.};
		    double v = f.evaluate(args);
		    E.info("evaluated func as " + v);
		    
		} catch (Exception ex) {
			E.error("cant parse " + ex);
		}
	
		
	}
	
}
