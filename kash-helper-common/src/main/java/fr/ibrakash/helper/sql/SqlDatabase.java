package fr.ibrakash.helper.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.*;

public abstract class SqlDatabase {

    protected final ExecutorService pool;
    protected final HikariDataSource source;

    public SqlDatabase(SqlCredential credential) {
        final HikariConfig config = new HikariConfig();
        config.setDriverClassName(credential.getDriverType().getDriverClassName());
        config.setJdbcUrl(String.format(credential.buildUrl(), credential.getHost(), credential.getPort(), credential.getDatabase()));
        config.setUsername(credential.getUser());
        config.setPassword(credential.getPassword());
        config.setMaximumPoolSize(credential.getPoolSize());
        config.setConnectionTimeout(30_000);
        this.source = new HikariDataSource(config);
        this.pool = new ThreadPoolExecutor(
                credential.getPoolSize(),
                credential.getPoolSize(),
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(1000),
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    public void shutdown() {
        this.pool.shutdownNow().forEach(Runnable::run);
    }

    protected Connection getConnection() {
        try {
            return this.source.getConnection();
        } catch (SQLException e) {
            throw new RuntimeException("Cannot get SQL connection", e);
        }
    }

    public void submitConnection(SqlConsumer<Connection> consumer) {
        pool.submit(() -> this.openConnection(consumer));
    }

    public <V> CompletableFuture<V> submitResultStatement(SqlResultStatement<Statement, V> consumer) {
        return this.submitResultStatement(null, consumer);
    }

    public <V> CompletableFuture<V> submitResultStatement(@Nullable CompletableFuture<V> inputFuture, SqlResultStatement<Statement, V> consumer) {
        CompletableFuture<V> future = inputFuture == null ? new CompletableFuture<>() : inputFuture;
        pool.execute(() -> this.completeResult(this.resultStatement(consumer), future));
        return future;
    }

    public <V> CompletableFuture<V> submitPreparedResult(String statement, SqlResultStatement<PreparedStatement, V> consumer) {
        return this.submitPreparedResult(null, statement, consumer);
    }

    public <V> CompletableFuture<V> submitPreparedResult(@Nullable CompletableFuture<V> inputFuture, String statement, SqlResultStatement<PreparedStatement, V> consumer) {
        CompletableFuture<V> future = inputFuture == null ? new CompletableFuture<>() : inputFuture;
        pool.execute(() -> this.completeResult(this.resultPreparedStatement(statement, consumer), future));
        return future;
    }

    private <V> void completeResult(@Nullable V result, CompletableFuture<V> future) {
        future.complete(result);
    }

    public void submitStatement(SqlStatement<Statement> consumer) {
        pool.execute(() -> this.createClosingStatement(consumer));
    }

    public void submitPreparedStatement(String statement, SqlStatement<PreparedStatement> consumer) {
        pool.execute(() -> this.prepareClosingStatement(statement, consumer));
    }

    public void openConnection(SqlConsumer<Connection> consumer) {
        try (final Connection connection = this.getConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <V> V resultPreparedStatement(String sql, SqlResultStatement<PreparedStatement, V> consumer) {
        try (final Connection connection = this.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement(sql)) {
                return consumer.result(statement);
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <V> V resultStatement(SqlResultStatement<Statement, V> consumer) {
        try (final Connection connection = this.getConnection()) {
            try (final Statement statement = connection.createStatement()) {
                return consumer.result(statement);
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public final void createClosingStatement(SqlStatement<Statement> consumer) {
        try (final Connection connection = this.getConnection()) {
            try (final Statement statement = connection.createStatement()) {
                consumer.execute(statement);
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public final void prepareClosingStatement(String preparedStatement, SqlStatement<PreparedStatement> consumer) {
        try (final Connection connection = this.getConnection()) {
            try (final PreparedStatement statement = connection.prepareStatement(preparedStatement)) {
                consumer.execute(statement);
            }
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public ExecutorService getPool() {
        return pool;
    }
}
