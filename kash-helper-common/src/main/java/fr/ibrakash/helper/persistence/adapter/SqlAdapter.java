package fr.ibrakash.helper.persistence.adapter;

import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.entity.PersistedId;

import java.lang.reflect.Field;

/**
 * Base class for SQL-backed adapters.
 *
 * <p>Provides the {@link #initTable} helper to create/init an entity store from
 * an annotated POJO class using the existing {@link EntityStore} annotation layer.
 *
 * <pre>{@code
 * public class MySqlAdapter extends SqlAdapter<MyRepository> {
 *
 *     public MySqlAdapter(MyRepository repository) {
 *         super(repository);
 *         // Tables are auto-created when you call initTable
 *         EntityStore<PlayerProfile, UUID> profiles = initTable(PlayerProfile.class);
 *         repository.loadEntireData(repository.getProfileCache(), profiles);
 *     }
 * }
 * }</pre>
 *
 * @param <R> the concrete repository type this adapter belongs to
 */
public abstract class SqlAdapter<R extends DatabaseRepository>
        extends BackendAdapter<R>
        implements SqlAdapterOperations {

    protected SqlAdapter(R repository) {
        super(repository);
    }

    /**
     * Creates (or opens) the SQL table for {@code entityClass} and returns the
     * annotation-driven {@link EntityStore} for it.
     *
     * <p>The id type is inferred automatically from the {@link PersistedId} field.
     *
     * @param entityClass the annotated entity class
     * @param <T>         entity type
     * @param <ID>        id type
     * @return a live {@link EntityStore} for the entity
     */
    @SuppressWarnings("unchecked")
    public <T, ID> EntityStore<T, ID> initTable(Class<T> entityClass) {
        Class<ID> idType = (Class<ID>) inferIdType(entityClass);
        return repository.entity(entityClass, idType);
    }

    /**
     * Creates (or opens) the SQL table with an explicit id type.
     */
    public <T, ID> EntityStore<T, ID> initTable(Class<T> entityClass, Class<ID> idType) {
        return repository.entity(entityClass, idType);
    }

    /**
     * Loads all rows from {@code store} into {@code target} using the entity id as key.
     */
    public <K, V> void loadEntireData(java.util.Map<K, V> target, EntityStore<V, K> store) {
        store.findAll().forEach(entity -> target.put(store.idOf(entity), entity));
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static Class<?> inferIdType(Class<?> entityType) {
        for (Field f : entityType.getDeclaredFields()) {
            if (f.getAnnotation(PersistedId.class) != null) return f.getType();
        }
        throw new IllegalArgumentException("No @PersistedId field found in " + entityType.getName());
    }
}
