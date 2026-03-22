package fr.ibrakash.helper.persistence.entity;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface PersistedBlob {

    /** Blob column name. Defaults to snake_case(fieldName). */
    String value() default "";

    PersistedBlobKind kind() default PersistedBlobKind.AUTO;

    int length() default -1;

    PersistedBlobTier blobTier() default PersistedBlobTier.NORMAL;

    /** Optional explicit serializer. If not set, defaults are inferred for primitive/wrapper/string/uuid/enum. */
    Class<? extends PersistedBlobSerializer<?>> serializer() default NoPersistedBlobSerializer.class;

    boolean nullable() default true;
}

