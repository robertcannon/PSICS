package org.psics.be;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface TextForm {

	double pos();
	String label();
	String ignore();
	
}

 