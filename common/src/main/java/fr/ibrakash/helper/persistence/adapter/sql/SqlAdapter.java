package fr.ibrakash.helper.persistence.adapter.sql;

import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.adapter.DatabaseAdapter;
import fr.ibrakash.helper.persistence.adapter.DatabaseRepository;
import fr.ibrakash.helper.persistence.entity.PersistedId;

import java.lang.reflect.Field;

public abstract class SqlAdapter<R extends DatabaseRepository>
        extends DatabaseAdapter<R>
        implements SqlAdapterOperations {

    protected SqlAdapter(R repository) {
        super(repository);
    }

    
    @SuppressWarnings("unchecked")
    public <T, ID> EntityStore<T, ID> initTable(Class<T> entityClass) {
        Class<ID> idType = (Class<ID>) inferIdType(entityClass);
        return repository.entity(entityClass, idType);
    }

    
    public <T, ID> EntityStore<T, ID> initTable(Class<T> entityClass, Class<ID> idType) {
        return repository.entity(entityClass, idType);
    }

    
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
