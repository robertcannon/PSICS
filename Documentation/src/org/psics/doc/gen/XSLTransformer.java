package org.psics.doc.gen;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.psics.be.E;


public class XSLTransformer {

	{
		System.setProperty("javax.xml.transform.TransformerFactory", 
					"net.sf.saxon.TransformerFactoryImpl");
	}
	
	 
	public XSLTransformer() {
		
	}
	
	public String transform(String srctext, InputStream transStream) {
		StreamSource ssrc = new StreamSource(new StringReader(srctext));
		StreamSource strans = new StreamSource(transStream);
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		
		transform(ssrc, strans, sr);
		
		return sw.toString();
	}
	
	public String transform(String srctext, File ftrans) {
		StreamSource ssrc = new StreamSource(new StringReader(srctext));
		StreamSource strans = new StreamSource(ftrans);
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		
		transform(ssrc, strans, sr);
		
		return sw.toString();
	}
	
	
	
	
	public String transform(String srctext, String transID) {
		StreamSource ssrc = new StreamSource(new StringReader(srctext));
		StreamSource strans = new StreamSource(transID);
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		
		transform(ssrc, strans, sr);
		
		return sw.toString();
	}
	
	
	
	
	
	public String transform(File fsrc, File ftrans) {
		StreamSource ssrc = new StreamSource(fsrc);
		StreamSource sxsl = new StreamSource(ftrans);
		
		StringWriter sw = new StringWriter();
		StreamResult sr = new StreamResult(sw);
		
		transform(ssrc, sxsl, sr);
		
		return sw.toString();
	}
		
	 
	
	

	public void transform(File fsrc, File ftrans, File fdest) {
		StreamSource ssrc = new StreamSource(fsrc);
		StreamSource sxsl = new StreamSource(ftrans);
		StreamResult sr = new StreamResult(fdest);
		transform(ssrc, sxsl, sr);
	}
	
	
	
	
	private void transform(StreamSource ssrc, StreamSource sxsl, StreamResult sres) {
		try {
			TransformerFactory tfactory = TransformerFactory.newInstance();
    		Transformer trf = tfactory.newTransformer(sxsl);
	    	trf.transform(ssrc, sres);
	    } catch (Exception ex) {
	    	E.error("transform failed " + ex);
	    	ex.printStackTrace();
	    }
	}
 
	
 
}
