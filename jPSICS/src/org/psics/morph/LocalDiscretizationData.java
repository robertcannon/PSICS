package org.psics.morph;
 

public class LocalDiscretizationData {

	String from;
	String to;
	double esize;
	
	
	
	public LocalDiscretizationData(String sfrom, String sto, double elementSize) {
		from = sfrom;
		to = sto;
		esize = elementSize;
	}


 


	public String getFrom() {
		return from;
	}
	
	public String getTo() {
		return to;
	}
	
	public double getEsize() {
		return esize;
	}

}
