package fr.ibrakash.helper.persistence.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistedIndex {

    /** Optional explicit index name. */
    String name() default "";

    /** Ordered list of columns/fields included in the index. */
    String[] columns();

    boolean unique() default false;
}

