package fr.ibrakash.helper.persistence.sql;

import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceSession;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.persistence.entity.PersistedId;

import java.lang.reflect.Field;

/**
 * Tiny helper to make SQL auto-table usage explicit and ergonomic.
 */
public final class SqlSerializer {

    private final PersistenceSession session;

    public SqlSerializer(PersistenceSession session) {
        this.session = session;
        if (session.backendType() != PersistenceType.SQL) {
            throw new IllegalStateException("SqlSerializer requires SQL backend, got " + session.backendType());
        }
    }

    /**
     * Explicit style wanted by users: initialize from entity class only.
     * The id type is inferred from the field annotated with {@link PersistedId}.
     */
    @SuppressWarnings("unchecked")
    public <T, ID> EntityStore<T, ID> initializeTable(Class<T> entityType) {
        Class<ID> idType = (Class<ID>) inferIdType(entityType);
        return this.session.entity(entityType, idType);
    }

    public <T, ID> EntityStore<T, ID> initializeTable(Class<T> entityType, Class<ID> idType) {
        return this.session.entity(entityType, idType);
    }

    public PersistenceEngine engine() {
        return this.session.engine();
    }

    private static Class<?> inferIdType(Class<?> entityType) {
        for (Field field : entityType.getDeclaredFields()) {
            if (field.getAnnotation(PersistedId.class) != null) {
                return field.getType();
            }
        }
        throw new IllegalArgumentException("No @PersistedId field found in " + entityType.getName());
    }
}

