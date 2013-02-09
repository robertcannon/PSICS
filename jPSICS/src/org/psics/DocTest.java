package org.psics;

import org.psics.be.E;
import org.psics.samples.rallpack3.RP3;
import org.psics.run.PSICSModel;

public class DocTest {

	
	public static void main(String[] argv) {
		
		PSICSModel pm = new PSICSModel(RP3.class, "run.xml");
		
		String s = pm.getTextVersion();
		
		E.info("text version for rp3:");
		E.info(s);
		
	}
	
	
	
}
