package fr.ibrakash.helper.persistence;

import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.persistence.json.JsonPersistenceProvider;
import fr.ibrakash.helper.persistence.mongo.MongoPersistenceProvider;
import fr.ibrakash.helper.persistence.sql.SqlPersistenceProvider;
import fr.ibrakash.helper.platform.KashAddon;

import java.util.EnumMap;
import java.util.Map;
import java.util.ServiceLoader;

public final class PersistenceManager {

    private PersistenceManager() {
    }

    public static PersistenceEngine create(KashAddon<?> addon, ConfigPersistence config) {
        ConfigPersistence effectiveConfig = config == null ? new ConfigPersistence() : config;
        Map<PersistenceType, PersistenceProvider> providers = loadProviders();

        for (PersistenceType type : effectiveConfig.orderedTypes()) {
            PersistenceProvider provider = providers.get(type);
            if (provider == null) continue;
            if (!provider.isAvailable(addon, effectiveConfig)) continue;
            return provider.create(addon, effectiveConfig);
        }

        throw new IllegalStateException("No persistence provider available for configured types: " + effectiveConfig.orderedTypes());
    }

    private static Map<PersistenceType, PersistenceProvider> loadProviders() {
        EnumMap<PersistenceType, PersistenceProvider> providers = new EnumMap<>(PersistenceType.class);

        // Built-in providers.
        registerIfAbsent(providers, new JsonPersistenceProvider());
        registerIfAbsent(providers, new SqlPersistenceProvider());
        registerIfAbsent(providers, new MongoPersistenceProvider());

        // Optional providers from addons/modules (e.g. MongoDB).
        ServiceLoader<PersistenceProvider> loader = ServiceLoader.load(PersistenceProvider.class);
        for (PersistenceProvider provider : loader) {
            registerIfAbsent(providers, provider);
        }

        return providers;
    }

    private static void registerIfAbsent(Map<PersistenceType, PersistenceProvider> providers, PersistenceProvider provider) {
        providers.putIfAbsent(provider.type(), provider);
    }
}
