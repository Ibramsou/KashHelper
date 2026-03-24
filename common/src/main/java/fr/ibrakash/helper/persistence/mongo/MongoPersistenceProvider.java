package fr.ibrakash.helper.persistence.mongo;

import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceProvider;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.platform.KashAddon;

public class MongoPersistenceProvider implements PersistenceProvider {

    @Override
    public PersistenceType type() {
        return PersistenceType.MONGODB;
    }

    @Override
    public boolean isAvailable(KashAddon<?> addon, ConfigPersistence config) {
        // If driver is not available at runtime this will fail on create and fallback chain applies.
        return config != null && config.getMongo() != null;
    }

    @Override
    public PersistenceEngine create(KashAddon<?> addon, ConfigPersistence config) {
        return new MongoPersistenceEngine(config.getMongo());
    }
}

