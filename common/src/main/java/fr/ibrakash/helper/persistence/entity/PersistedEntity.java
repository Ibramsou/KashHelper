package fr.ibrakash.helper.persistence.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistedEntity {

    /** Optional explicit table/file name. Falls back to class name if empty. */
    String value() default "";
}

