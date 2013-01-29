 
package org.psics.quantity.annotation;

import java.lang.annotation.*;

import org.psics.quantity.units.Units;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Quantity {
	
	Units units();
	
	String range();
	
	String tag();

	boolean required();

}
