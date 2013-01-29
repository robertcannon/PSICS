
package org.psics.quantity.annotation;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.TYPE })
public @interface ModelType {

	String tag();

	String info();

	boolean standalone();

	Class<?>[] usedWithin();

}
