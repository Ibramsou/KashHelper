package fr.ibrakash.helper.persistence.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PersistedColumn {

    /** Optional explicit column name. */
    String value() default "";

    boolean nullable() default true;

    /** Applied for SQL only when non-empty. Example: "0", "'DEFAULT'" */
    String defaultValue() default "";

    /** SQL varchar length for string-like fields; ignored for non-string types. */
    int length() default 255;
}

