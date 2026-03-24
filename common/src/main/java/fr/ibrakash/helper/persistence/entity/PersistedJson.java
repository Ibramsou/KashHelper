package fr.ibrakash.helper.persistence.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Overrides JSON storage strategy for an entity.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistedJson {

    PersistedJsonMode mode() default PersistedJsonMode.AUTO;
}

