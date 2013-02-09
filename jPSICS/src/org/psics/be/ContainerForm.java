package org.psics.be;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface ContainerForm {

	double pos();
	
	String label();
	
	boolean unwrapone();
	
}

 