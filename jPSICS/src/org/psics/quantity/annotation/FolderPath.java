 
package org.psics.quantity.annotation;

import java.lang.annotation.*;
 

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface FolderPath {

	String tag();
 
	boolean required();
	
	String fallback();
	
}
