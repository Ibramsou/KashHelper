package fr.ibrakash.helper.persistence;

import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.platform.KashAddon;

public interface PersistenceProvider {

    PersistenceType type();

    default boolean isAvailable(KashAddon<?> addon, ConfigPersistence config) {
        return true;
    }

    PersistenceEngine create(KashAddon<?> addon, ConfigPersistence config);
}

