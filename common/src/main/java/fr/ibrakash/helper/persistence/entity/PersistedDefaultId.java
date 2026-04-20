package fr.ibrakash.helper.persistence.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PersistedDefaultId {

    /**
     * Column name that should be promoted as the entity id when no field uses @PersistedId.
     */
    String value();
}



