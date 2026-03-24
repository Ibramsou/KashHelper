package fr.ibrakash.helper.persistence.sql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.ibrakash.helper.configuration.objects.database.ConfigSql;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.sql.SqlDatabase;
import fr.ibrakash.helper.sql.SqlDriverType;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class SqlPersistenceEngine implements PersistenceEngine {

    private final SqlDriverType driverType;
    private final ManagedSqlDatabase database;

    public SqlPersistenceEngine(ConfigSql config) {
        this.driverType = config.getDriverType();
        this.database = new ManagedSqlDatabase(config);
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

        private final ExecutorService pool;
        private final HikariDataSource source;

        ManagedSqlDatabase(ConfigSql credential) {
            HikariConfig config = new HikariConfig();
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

        @Override
        protected Connection getConnection() throws SQLException {
            return this.source.getConnection();
        }

        @Override
        public ExecutorService getPool() {
            return this.pool;
        }

        @Override
        public DataSource getDataSource() {
            return this.source;
        }

        @Override
        public void shutdown() {
            this.pool.shutdownNow().forEach(Runnable::run);
            this.source.close();
        }
    }
}
