package fr.ibrakash.helper.persistence.sql;

import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.sql.SqlDatabase;
import org.jdbi.v3.core.Jdbi;

/**
 * Optional bridge to JDBI on top of the existing SQL engine.
 *
 * <p>This keeps SQL statements fully explicit while removing JDBC boilerplate
 * (binding/mapping helpers, handle management, etc.).
 */
public final class JdbiSupport {

    private JdbiSupport() {
    }

    public static Jdbi fromDatabase(SqlDatabase database) {
        return Jdbi.create(database.getDataSource());
    }

    public static Jdbi fromEngine(PersistenceEngine engine) {
        if (engine.type() != PersistenceType.SQL) {
            throw new IllegalStateException("JDBI is only available for SQL backend, got: " + engine.type());
        }
        return fromDatabase(engine.getSqlDatabase());
    }
}

