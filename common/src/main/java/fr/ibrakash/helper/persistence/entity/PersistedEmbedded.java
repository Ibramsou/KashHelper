package fr.ibrakash.helper.persistence.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PersistedEmbedded {

    String prefix() default "";

    /**
     * If true, the embedded object will be null after deserialization
     * when ALL its columns contain default/empty values.
     * Default is false (always instantiate the embedded object).
     */
    boolean nullable() default false;
}

