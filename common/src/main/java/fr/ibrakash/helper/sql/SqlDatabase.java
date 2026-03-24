package fr.ibrakash.helper.sql;

import org.jetbrains.annotations.Nullable;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

public abstract class SqlDatabase {

    protected abstract Connection getConnection() throws SQLException;

    public abstract ExecutorService getPool();

    public abstract DataSource getDataSource();

    public abstract void shutdown();

    public void submitConnection(SqlConsumer<Connection> consumer) {
        getPool().submit(() -> this.openConnection(consumer));
    }

    public <V> CompletableFuture<V> submitResultStatement(SqlResultStatement<Statement, V> consumer) {
        return this.submitResultStatement(null, consumer);
    }

    public <V> CompletableFuture<V> submitResultStatement(@Nullable CompletableFuture<V> inputFuture, SqlResultStatement<Statement, V> consumer) {
        CompletableFuture<V> future = inputFuture == null ? new CompletableFuture<>() : inputFuture;
        getPool().execute(() -> this.completeResult(this.resultStatement(consumer), future));
        return future;
    }

    public <V> CompletableFuture<V> submitPreparedResult(String statement, SqlResultStatement<PreparedStatement, V> consumer) {
        return this.submitPreparedResult(null, statement, consumer);
    }

    public <V> CompletableFuture<V> submitPreparedResult(@Nullable CompletableFuture<V> inputFuture, String statement, SqlResultStatement<PreparedStatement, V> consumer) {
        CompletableFuture<V> future = inputFuture == null ? new CompletableFuture<>() : inputFuture;
        getPool().execute(() -> this.completeResult(this.resultPreparedStatement(statement, consumer), future));
        return future;
    }

    private <V> void completeResult(@Nullable V result, CompletableFuture<V> future) {
        future.complete(result);
    }

    public void submitStatement(SqlStatement<Statement> consumer) {
        getPool().execute(() -> this.createClosingStatement(consumer));
    }

    public void submitPreparedStatement(String statement, SqlStatement<PreparedStatement> consumer) {
        getPool().execute(() -> this.prepareClosingStatement(statement, consumer));
    }

    public void openConnection(SqlConsumer<Connection> consumer) {
        try (Connection connection = this.getConnection()) {
            consumer.accept(connection);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public <V> V resultPreparedStatement(String sql, SqlResultStatement<PreparedStatement, V> consumer) {
        try (Connection connection = this.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return consumer.result(statement);
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public <V> V resultStatement(SqlResultStatement<Statement, V> consumer) {
        try (Connection connection = this.getConnection();
             Statement statement = connection.createStatement()) {
            return consumer.result(statement);
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public final void createClosingStatement(SqlStatement<Statement> consumer) {
        try (Connection connection = this.getConnection();
             Statement statement = connection.createStatement()) {
            consumer.execute(statement);
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public final void prepareClosingStatement(String preparedStatement, SqlStatement<PreparedStatement> consumer) {
        try (Connection connection = this.getConnection();
             PreparedStatement statement = connection.prepareStatement(preparedStatement)) {
            consumer.execute(statement);
        } catch (SQLException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
