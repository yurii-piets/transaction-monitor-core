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
     * Specifies path to database configuration property file
     */
    String path() default "";

    /**
     * Specifies qualifiers of each database
     * Put all prefixes for database configuration properties
     * Also all databases is reachable by specified qualifier
     */
    String[] qualifiers() default {};
}
