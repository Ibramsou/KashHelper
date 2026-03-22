package fr.ibrakash.helper.persistence.sql;
import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceProvider;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.platform.KashAddon;
public class SqlPersistenceProvider implements PersistenceProvider {
    @Override
    public PersistenceType type() {
        return PersistenceType.SQL;
    }
    @Override
    public PersistenceEngine create(KashAddon<?> addon, ConfigPersistence config) {
        return new SqlPersistenceEngine(config.getSql());
    }
}
