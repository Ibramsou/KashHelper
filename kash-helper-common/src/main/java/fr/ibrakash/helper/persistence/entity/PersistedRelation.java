package fr.ibrakash.helper.persistence.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PersistedRelation {

    /** Relation table name. */
    String table();

    /** Join column in relation table; defaults to entity id column when empty. */
    String joinColumn() default "";

    /**
     * Value column for primitive/simple relation elements.
     * Optional for object elements; when empty all serializable fields are mapped as columns.
     */
    String valueColumn() default "";

    /** Optional prefix for generated object columns when valueColumn is empty. */
    String prefix() default "";

    /** Order column for list-like relations. */
    String orderColumn() default "order_index";
}

