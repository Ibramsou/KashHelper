package fr.ibrakash.helper.persistence.sql;

import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.Repository;
import fr.ibrakash.helper.sql.SqlDatabase;
import fr.ibrakash.helper.sql.SqlDriverType;

/**
 * Base class for SQL-backed repositories.
 *
 * <p>Subclasses receive the live {@link SqlDatabase} and {@link SqlDriverType} and
 * write their own statements using {@link fr.ibrakash.helper.sql.SqlDialectStatement}.
 *
 * <p>Example skeleton:
 * <pre>{@code
 * public class MyRepository extends SqlRepository {
 *
 *     private static final SqlDialectStatement CREATE_TABLE = SqlDialectStatement
 *             .builder("CREATE TABLE IF NOT EXISTS my_table (id VARCHAR(36) PRIMARY KEY, value TEXT)")
 *             .sqlite("CREATE TABLE IF NOT EXISTS my_table (id TEXT PRIMARY KEY, value TEXT)")
 *             .build();
 *
 *     private static final SqlDialectStatement UPSERT = SqlDialectStatement
 *             .builder("INSERT INTO my_table (id, value) VALUES (?, ?) ON DUPLICATE KEY UPDATE value = VALUES(value)")
 *             .postgresql("INSERT INTO my_table (id, value) VALUES (?, ?) ON CONFLICT (id) DO UPDATE SET value = EXCLUDED.value")
 *             .sqlite("INSERT OR REPLACE INTO my_table (id, value) VALUES (?, ?)")
 *             .build();
 *
 *     @Override
 *     protected void onCreate() {
 *         execute(stmt -> stmt.executeUpdate(CREATE_TABLE.resolve(driverType)));
 *     }
 *
 *     @Override
 *     public void loadAll() {
 *         prepare("SELECT id, value FROM my_table", stmt -> {
 *             ResultSet rs = stmt.executeQuery();
 *             while (rs.next()) {
 *                 cache.put(rs.getString("id"), rs.getString("value"));
 *             }
 *         });
 *     }
 *
 *     @Override
 *     public void saveAll() {
 *         cache.forEach((id, value) -> prepare(UPSERT.resolve(driverType), stmt -> {
 *             stmt.setString(1, id);
 *             stmt.setString(2, value);
 *             stmt.executeUpdate();
 *         }));
 *     }
 * }
 * }</pre>
 */
public abstract class SqlRepository implements Repository {

    protected SqlDatabase database;
    protected SqlDriverType driverType;

    @Override
    public final void init(PersistenceEngine engine) {
        this.database  = engine.getSqlDatabase();
        this.driverType = engine.getDriverType();
        this.onCreate();
    }

    /**
     * Called once after the SQL database is injected.
     * Override to run {@code CREATE TABLE IF NOT EXISTS} and similar setup statements.
     */
    protected abstract void onCreate();

    /** Convenience: run a plain (non-prepared) statement synchronously. */
    protected void execute(fr.ibrakash.helper.sql.SqlStatement<java.sql.Statement> consumer) {
        this.database.createClosingStatement(consumer);
    }

    /** Convenience: run a prepared statement synchronously. */
    protected void prepare(String sql, fr.ibrakash.helper.sql.SqlStatement<java.sql.PreparedStatement> consumer) {
        this.database.prepareClosingStatement(sql, consumer);
    }

    /** Convenience: run a prepared statement and return a result synchronously. */
    protected <V> V query(String sql, fr.ibrakash.helper.sql.SqlResultStatement<java.sql.PreparedStatement, V> consumer) {
        return this.database.resultPreparedStatement(sql, consumer);
    }
}

