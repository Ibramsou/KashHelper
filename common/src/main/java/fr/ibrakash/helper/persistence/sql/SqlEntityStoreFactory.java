package fr.ibrakash.helper.persistence.sql;

import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.EntityStoreFactory;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.persistence.entity.internal.SqlEntityStore;

public final class SqlEntityStoreFactory implements EntityStoreFactory {

    @Override
    public PersistenceType type() {
        return PersistenceType.SQL;
    }

    @Override
    public <T, ID> EntityStore<T, ID> create(PersistenceEngine engine, Class<T> entityType, Class<ID> idType) {
        return new SqlEntityStore<>(engine, entityType, idType);
    }
}

