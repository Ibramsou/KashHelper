package fr.ibrakash.helper.persistence;

import fr.ibrakash.helper.sql.SqlDatabase;
import fr.ibrakash.helper.sql.SqlDriverType;

import java.io.File;

/**
 * Gives access to the raw resources of the chosen backend.
 * MongoDB and other optional backends are injected via {@link PersistenceProvider}
 * and expose the same contract.
 */
public interface PersistenceEngine extends AutoCloseable {

    PersistenceType type();

    /**
     * Returns the connected {@link SqlDatabase} for SQL backends.
     * Throws {@link UnsupportedOperationException} for non-SQL backends.
     */
    default SqlDatabase getSqlDatabase() {
        throw new UnsupportedOperationException("getSqlDatabase() is not available for backend " + type());
    }

    /**
     * Returns the SQL driver type so statements can be resolved with
     * {@link fr.ibrakash.helper.sql.SqlDialectStatement#resolve(SqlDriverType)}.
     * Throws {@link UnsupportedOperationException} for non-SQL backends.
     */
    default SqlDriverType getDriverType() {
        throw new UnsupportedOperationException("getDriverType() is not available for backend " + type());
    }

    /**
     * Returns the base storage folder for file-based backends (JSON, etc.).
     * Throws {@link UnsupportedOperationException} for non-file backends.
     */
    default File getStorageFolder() {
        throw new UnsupportedOperationException("getStorageFolder() is not available for backend " + type());
    }

    @Override
    default void close() {
    }
}
