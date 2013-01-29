
package org.psics.quantity.annotation;

import java.lang.annotation.*;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Metadata {

	String tag();

	String info();


}
