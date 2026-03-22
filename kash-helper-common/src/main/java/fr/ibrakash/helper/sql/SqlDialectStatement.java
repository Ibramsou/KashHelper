package fr.ibrakash.helper.sql;

import java.util.EnumMap;
import java.util.Map;

public final class SqlDialectStatement {

    private final String defaultSql;
    private final Map<SqlDriverType, String> overrides;

    private SqlDialectStatement(String defaultSql, Map<SqlDriverType, String> overrides) {
        this.defaultSql = defaultSql;
        this.overrides = overrides;
    }

    public static Builder builder(String defaultSql) {
        return new Builder(defaultSql);
    }

    public String resolve(SqlDriverType type) {
        return this.overrides.getOrDefault(type, this.defaultSql);
    }

    public static final class Builder {

        private final String defaultSql;
        private final Map<SqlDriverType, String> overrides = new EnumMap<>(SqlDriverType.class);

        private Builder(String defaultSql) {
            this.defaultSql = defaultSql;
        }

        public Builder mysql(String sql) {
            this.overrides.put(SqlDriverType.MYSQL, sql);
            return this;
        }

        public Builder mariadb(String sql) {
            this.overrides.put(SqlDriverType.MARIADB, sql);
            return this;
        }

        public Builder postgresql(String sql) {
            this.overrides.put(SqlDriverType.POSTGRESQL, sql);
            return this;
        }

        public Builder sqlite(String sql) {
            this.overrides.put(SqlDriverType.SQLITE, sql);
            return this;
        }

        public SqlDialectStatement build() {
            return new SqlDialectStatement(this.defaultSql, this.overrides);
        }
    }
}

