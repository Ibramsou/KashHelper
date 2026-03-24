package fr.ibrakash.helper.persistence.sql;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.ibrakash.helper.sql.SqlDriverType;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Function;

/**
 * Caches dynamic SQL templates keyed by pattern + arity.
 *
 * <p>This is intended for SQL strings whose placeholder count depends on runtime input,
 * such as {@code IN (?, ?, ...)} queries for bulk loads.
 */
public final class SqlStatementTemplateCache {

    private static final Duration EXPIRATION = Duration.ofHours(6);

    private final Cache<Key, String> cache = Caffeine.newBuilder()
            .expireAfterWrite(EXPIRATION)
            .build();

    public String getOrCompute(String kind,
                               String namespace,
                               String signature,
                               int arity,
                               SqlDriverType driver,
                               Function<Integer, String> builder) {
        return this.cache.get(new Key(kind, namespace, signature, arity, driver), key -> builder.apply(arity));
    }

    public record Key(String kind, String namespace, String signature, int arity, SqlDriverType driver) {
        public Key {
            Objects.requireNonNull(kind, "kind");
            Objects.requireNonNull(namespace, "namespace");
            Objects.requireNonNull(signature, "signature");
            Objects.requireNonNull(driver, "driver");
            if (arity < 0) throw new IllegalArgumentException("arity must be >= 0");
        }
    }
}

