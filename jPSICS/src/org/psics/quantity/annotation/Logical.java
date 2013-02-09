 
package org.psics.quantity.annotation;

import java.lang.annotation.*;



@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Logical {
	
  
	String tag();

	boolean required();

}
