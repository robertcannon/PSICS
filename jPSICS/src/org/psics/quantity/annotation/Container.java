
package org.psics.quantity.annotation;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Container {

	String tag();

	Class<?>[] contentTypes();

}
