package fr.ibrakash.helper.persistence.adapter;

import fr.ibrakash.helper.sql.SqlConsumer;
import fr.ibrakash.helper.sql.SqlDatabase;
import fr.ibrakash.helper.sql.SqlDriverType;
import fr.ibrakash.helper.sql.SqlResultStatement;
import fr.ibrakash.helper.sql.SqlStatement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

/**
 * Common SQL operation mixin for adapters.
 *
 * <p>Implementors only need to provide {@link #repository()}.
 */
public interface SqlAdapterOperations {

    DatabaseRepository repository();

    default SqlDatabase sqlDatabase() {
        return repository().sqlDatabase();
    }

    default SqlDriverType sqlDriver() {
        return repository().sqlDriver();
    }

    default ExecutorService sqlPool() {
        return sqlDatabase().getPool();
    }

    default DataSource sqlDataSource() {
        return sqlDatabase().getDataSource();
    }

    default void openConnection(SqlConsumer<Connection> consumer) {
        sqlDatabase().openConnection(consumer);
    }

    default void execute(SqlStatement<Statement> consumer) {
        sqlDatabase().createClosingStatement(consumer);
    }

    default void prepare(String sql, SqlStatement<PreparedStatement> consumer) {
        sqlDatabase().prepareClosingStatement(sql, consumer);
    }

    default <V> V query(SqlResultStatement<Statement, V> consumer) {
        return sqlDatabase().resultStatement(consumer);
    }

    default <V> V query(String sql, SqlResultStatement<PreparedStatement, V> consumer) {
        return sqlDatabase().resultPreparedStatement(sql, consumer);
    }

    default void submitExecute(SqlStatement<Statement> consumer) {
        sqlDatabase().submitStatement(consumer);
    }

    default void submitPrepare(String sql, SqlStatement<PreparedStatement> consumer) {
        sqlDatabase().submitPreparedStatement(sql, consumer);
    }

    default <V> CompletableFuture<V> submitQuery(SqlResultStatement<Statement, V> consumer) {
        return sqlDatabase().submitResultStatement(consumer);
    }

    default <V> CompletableFuture<V> submitQuery(String sql, SqlResultStatement<PreparedStatement, V> consumer) {
        return sqlDatabase().submitPreparedResult(sql, consumer);
    }
}
