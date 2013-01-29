 
package org.psics.quantity.annotation;

import java.lang.annotation.*;
 

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface LibraryPath {

	String tag();
 
	boolean required();
	
	String fallback();
	
}
