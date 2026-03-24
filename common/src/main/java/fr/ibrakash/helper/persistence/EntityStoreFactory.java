package fr.ibrakash.helper.persistence;

public interface EntityStoreFactory {

    PersistenceType type();

    <T, ID> EntityStore<T, ID> create(PersistenceEngine engine, Class<T> entityType, Class<ID> idType);
}

