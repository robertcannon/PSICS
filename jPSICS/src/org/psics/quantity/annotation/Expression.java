
package org.psics.quantity.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.psics.quantity.units.Units;


@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.FIELD })
public @interface Expression {


	String tag();

	boolean required();

	Units units();

}
