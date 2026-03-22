package fr.ibrakash.helper.persistence.sql;

import fr.ibrakash.helper.configuration.objects.database.ConfigSql;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.sql.SqlDatabase;
import fr.ibrakash.helper.sql.SqlDriverType;

public class SqlPersistenceEngine implements PersistenceEngine {

    private final SqlDriverType driverType;
    private final ManagedSqlDatabase database;

    public SqlPersistenceEngine(ConfigSql config) {
        this.driverType = config.getDriverType();
        this.database   = new ManagedSqlDatabase(config);
    }

    @Override
    public PersistenceType type() {
        return PersistenceType.SQL;
    }

    @Override
    public SqlDatabase getSqlDatabase() {
        return this.database;
    }

    @Override
    public SqlDriverType getDriverType() {
        return this.driverType;
    }

    @Override
    public void close() {
        this.database.shutdown();
    }

    private static final class ManagedSqlDatabase extends SqlDatabase {
        ManagedSqlDatabase(ConfigSql credential) {
            super(credential);
        }
    }
}
