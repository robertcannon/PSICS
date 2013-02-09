
package org.psics.quantity.annotation;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface ReferenceByIdentifier {

	String tag();

	Class<?>[] targetTypes();

	boolean required();

  	Location location();

}
