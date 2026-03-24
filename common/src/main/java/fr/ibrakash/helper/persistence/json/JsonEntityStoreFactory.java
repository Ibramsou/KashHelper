package fr.ibrakash.helper.persistence.json;

import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.EntityStoreFactory;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.persistence.entity.internal.JsonEntityStore;

public final class JsonEntityStoreFactory implements EntityStoreFactory {

    @Override
    public PersistenceType type() {
        return PersistenceType.JSON;
    }

    @Override
    public <T, ID> EntityStore<T, ID> create(PersistenceEngine engine, Class<T> entityType, Class<ID> idType) {
        return new JsonEntityStore<>(engine, entityType, idType);
    }
}

