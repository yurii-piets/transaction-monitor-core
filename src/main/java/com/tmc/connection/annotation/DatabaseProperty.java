package com.tmc.connection.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Put this annotation on class to define path to configuration file
 * eg. @DatabaseProperty("database.properties")
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DatabaseProperty {

    /**
     * Put as value path to database configuration file
     */
    String value() default "";

    String[] qualifiers() default {};
}
