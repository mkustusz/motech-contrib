package org.motechproject.validation.constraints;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target({FIELD})
@Retention(RUNTIME)
public @interface DateTimeFormat {
    String pattern() default "dd/MM/YYYY HH:mm:ss";
    boolean validateEmptyString() default  true;
}
