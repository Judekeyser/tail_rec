package be.jdevelopment.tailrec.lib.threading;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TailRecursiveExecutor {

    String parameter() default "Hello";

}
