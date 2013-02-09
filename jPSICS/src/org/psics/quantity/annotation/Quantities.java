 
package org.psics.quantity.annotation;

import java.lang.annotation.*;

import org.psics.quantity.units.Units;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Quantities {
 
	
	String tag();

	String info();
	
	boolean required();
	
	Units units();
	
	String range();
	
}
