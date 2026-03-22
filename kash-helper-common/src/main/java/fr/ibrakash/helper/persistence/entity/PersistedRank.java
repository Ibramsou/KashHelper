package fr.ibrakash.helper.persistence.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares a computed rank field for leaderboard-like ordering.
 *
 * <p>The annotated field must be {@code transient}; its value is computed at runtime
 * from {@link #sort_columns()} and is never persisted directly.</p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PersistedRank {

    /** Sort clauses, for example: {@code {"score DESC", "points DESC"}}. */
    String[] sort_columns();

    /** Whether this rank should be populated during entity deserialization. */
    boolean load_on_deserialize() default false;
}

