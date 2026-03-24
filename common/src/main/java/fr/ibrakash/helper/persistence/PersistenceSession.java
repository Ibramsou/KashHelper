package fr.ibrakash.helper.persistence;

import com.mongodb.client.MongoDatabase;
import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.persistence.entity.internal.JsonEntityStore;
import fr.ibrakash.helper.persistence.entity.internal.SqlEntityStore;
import fr.ibrakash.helper.persistence.mongo.MongoPersistenceEngine;
import fr.ibrakash.helper.platform.KashAddon;
import fr.ibrakash.helper.sql.SqlDatabase;
import fr.ibrakash.helper.sql.SqlDriverType;
import fr.ibrakash.helper.sql.SqlResultStatement;
import fr.ibrakash.helper.sql.SqlStatement;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class PersistenceSession implements AutoCloseable {

    private final PersistenceEngine engine;

    protected PersistenceSession(PersistenceEngine engine) {
        this.engine = engine;
    }

    protected PersistenceSession(KashAddon<?> addon, ConfigPersistence config) {
        this(PersistenceManager.create(addon, config));
    }

    public static PersistenceSession create(KashAddon<?> addon, ConfigPersistence config) {
        return new PersistenceSession(PersistenceManager.create(addon, config));
    }

    public <T, ID> EntityStore<T, ID> entity(Class<T> entityType, Class<ID> idType) {
        if (this.engine.type() == PersistenceType.SQL) {
            return new SqlEntityStore<>(this.engine, entityType, idType);
        }
        if (this.engine.type() == PersistenceType.JSON) {
            return new JsonEntityStore<>(this.engine, entityType, idType);
        }
        throw new UnsupportedOperationException("No generic EntityStore implementation for backend " + this.engine.type());
    }

    public PersistenceType backendType() {
        return this.engine.type();
    }

    public SqlDatabase sqlDatabase() {
        return this.engine.getSqlDatabase();
    }

    public SqlDriverType sqlDriver() {
        return this.engine.getDriverType();
    }

    public File storageFolder() {
        return this.engine.getStorageFolder();
    }

    public MongoDatabase mongoDatabase() {
        if (this.engine instanceof MongoPersistenceEngine mongo) {
            return mongo.getMongoDatabase();
        }
        throw new UnsupportedOperationException("mongoDatabase() is not available for backend " + this.engine.type());
    }

    public void execute(SqlStatement<Statement> consumer) {
        this.sqlDatabase().createClosingStatement(consumer);
    }

    public void prepare(String sql, SqlStatement<PreparedStatement> consumer) {
        this.sqlDatabase().prepareClosingStatement(sql, consumer);
    }

    public <V> V query(SqlResultStatement<Statement, V> consumer) {
        return this.sqlDatabase().resultStatement(consumer);
    }

    public <V> V query(String sql, SqlResultStatement<PreparedStatement, V> consumer) {
        return this.sqlDatabase().resultPreparedStatement(sql, consumer);
    }

    /** @deprecated Prefer direct helpers like {@link #sqlDatabase()} / {@link #query(String, SqlResultStatement)}. */
    @Deprecated(forRemoval = false)
    public PersistenceEngine engine() {
        return this.engine;
    }

    @Override
    public void close() {
        this.engine.close();
    }
}
