package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.binary.BinaryStorage;
import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.entity.PersistedBlobKind;
import fr.ibrakash.helper.persistence.entity.PersistedBlobTier;
import fr.ibrakash.helper.persistence.query.SortClause;
import fr.ibrakash.helper.persistence.query.SortDirection;
import fr.ibrakash.helper.persistence.query.SortQuery;
import fr.ibrakash.helper.persistence.sql.SqlStatementTemplateCache;
import fr.ibrakash.helper.sql.SqlDialectStatement;
import fr.ibrakash.helper.sql.SqlDriverType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

public final class SqlEntityStore<T, ID> implements EntityStore<T, ID> {

    private static final byte[] EMPTY_BYTES = new byte[0];
    private static final SqlStatementTemplateCache TEMPLATE_CACHE = new SqlStatementTemplateCache();

    private record RelationSql(
            SqlDialectStatement createTable,
            String insert,
            String deleteById,
            String bulkDeletePrefix,
            String bulkDeleteSuffix,
            String bulkDeleteSignature,
            String bulkSelectPrefix,
            String bulkSelectSuffix,
            String bulkSelectSignature
    ) {}

    private final EntityModel<T, ID> model;
    private final fr.ibrakash.helper.sql.SqlDatabase database;
    private final SqlDriverType driverType;

    private final SqlDialectStatement createTable;
    private final List<EntityModel.IndexDef> createIndexes;
    private final String selectProjection;
    private final String selectById;
    private final String selectAll;
    private final SqlDialectStatement upsert;
    private final String deleteById;
    private final Map<String, RelationSql> relationSql;

    public SqlEntityStore(PersistenceEngine engine, Class<T> entityType, Class<ID> idType) {
        this.model = EntityModel.from(entityType, idType);
        this.database = engine.getSqlDatabase();
        this.driverType = engine.getDriverType();

        this.createTable = buildCreateTable();
        this.createIndexes = buildCreateIndexes();
        this.selectProjection = buildSelectProjection();
        this.selectById = buildSelectById();
        this.selectAll = buildSelectAll();
        this.upsert = buildUpsert();
        this.deleteById = buildDeleteById();
        this.relationSql = buildRelationSql();

        this.init();
    }

    private void init() {
        this.database.createClosingStatement(stmt -> stmt.executeUpdate(this.createTable.resolve(this.driverType)));
        for (EntityModel.IndexDef index : this.createIndexes) {
            this.createIndexIfMissing(index);
        }
        for (RelationSql sql : this.relationSql.values()) {
            this.database.createClosingStatement(stmt -> stmt.executeUpdate(sql.createTable().resolve(this.driverType)));
        }
    }

    @Override
    public Optional<T> find(ID id) {
        T entity = this.database.resultPreparedStatement(this.selectById, stmt -> {
            EntityTypeMapper.bind(stmt, 1, this.model.idType(), id);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return null;
            try {
                return mapRow(rs, null);
            } catch (Exception e) {
                throw new RuntimeException("Unable to map SQL row to " + this.model.entityType().getSimpleName(), e);
            }
        });

        if (entity != null) {
            loadRelationsBatch(List.of(entity));
        }
        return Optional.ofNullable(entity);
    }

    @Override
    public List<T> findAll() {
        List<T> out = this.database.resultStatement(stmt -> {
            List<T> loaded = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(this.selectAll);
            while (rs.next()) {
                try {
                    loaded.add(mapRow(rs, null));
                } catch (Exception e) {
                    throw new RuntimeException("Unable to map SQL row to " + this.model.entityType().getSimpleName(), e);
                }
            }
            return loaded;
        });

        loadRelationsBatch(out);
        return out;
    }

    @Override
    public List<T> findAll(List<SortClause> sorts, int limit) {
        String sql = selectAllSortedSql(sorts, 0, limit, null);
        List<T> out = this.database.resultStatement(stmt -> {
            List<T> loaded = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                try {
                    loaded.add(mapRow(rs, null));
                } catch (Exception e) {
                    throw new RuntimeException("Unable to map SQL row to " + this.model.entityType().getSimpleName(), e);
                }
            }
            return loaded;
        });

        loadRelationsBatch(out);
        return out;
    }

    @Override
    public List<T> findAllSorted(SortQuery query) {
        Set<String> columns = query.hasColumns() ? resolveColumns(query.columns()) : null;
        String sql = selectAllSortedSql(query.sorts(), query.offset(), query.limit(), columns);

        List<T> out = this.database.resultStatement(stmt -> {
            List<T> loaded = new ArrayList<>();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                try {
                    loaded.add(mapRow(rs, columns));
                } catch (Exception e) {
                    throw new RuntimeException("Unable to map SQL row to " + this.model.entityType().getSimpleName(), e);
                }
            }
            return loaded;
        });

        if (columns == null) {
            loadRelationsBatch(out);
        }
        return out;
    }

    @Override
    public List<T> findAllByIds(List<ID> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }

        String sql = selectByIdsSql(ids.size());
        Map<ID, T> byId = this.database.resultPreparedStatement(sql, stmt -> {
            int index = 1;
            for (ID id : ids) {
                EntityTypeMapper.bind(stmt, index++, this.model.idType(), id);
            }

            ResultSet rs = stmt.executeQuery();
            Map<ID, T> loaded = new LinkedHashMap<>();
            while (rs.next()) {
                try {
                    T entity = mapRow(rs, null);
                    loaded.put(this.model.idOf(entity), entity);
                } catch (Exception e) {
                    throw new RuntimeException("Unable to map SQL row to " + this.model.entityType().getSimpleName(), e);
                }
            }
            return loaded;
        });

        List<T> ordered = new ArrayList<>(ids.size());
        for (ID id : ids) {
            T entity = byId.get(id);
            if (entity != null) {
                ordered.add(entity);
            }
        }

        loadRelationsBatch(ordered);
        return ordered;
    }

    @Override
    public void save(T entity) {
        this.saveAll(List.of(entity));
    }

    @Override
    public void saveAll(Iterable<T> entities) {
        List<T> batch = normalizeEntities(entities);
        if (batch.isEmpty()) {
            return;
        }

        this.database.prepareClosingStatement(this.upsert.resolve(this.driverType), stmt -> {
            for (T entity : batch) {
                bindEntity(stmt, entity);
                stmt.addBatch();
            }
            stmt.executeBatch();
        });

        this.saveRelationsBatch(batch);
    }

    @Override
    public void delete(ID id) {
        this.deleteRelations(id);
        this.database.prepareClosingStatement(this.deleteById, stmt -> {
            EntityTypeMapper.bind(stmt, 1, this.model.idType(), id);
            stmt.executeUpdate();
        });
    }

    @Override
    public ID idOf(T entity) {
        return this.model.idOf(entity);
    }

    @Override
    public int rankOf(ID id, List<SortClause> sorts) {
        if (id == null) return -1;
        Map<ID, Integer> ranks = this.ranksOf(List.of(id), sorts);
        return ranks.getOrDefault(id, -1);
    }

    @Override
    public Map<ID, Integer> ranksOf(List<ID> ids, List<SortClause> sorts) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }

        String sql = rankByIdsSql(sorts, ids.size());
        return this.database.resultPreparedStatement(sql, stmt -> {
            int index = 1;
            for (ID id : ids) {
                EntityTypeMapper.bind(stmt, index++, this.model.idType(), id);
            }

            ResultSet rs = stmt.executeQuery();
            Map<ID, Integer> out = new LinkedHashMap<>();
            while (rs.next()) {
                @SuppressWarnings("unchecked")
                ID id = (ID) EntityTypeMapper.read(rs, this.model.idColumn().name(), this.model.idType());
                out.put(id, rs.getInt("rank_value"));
            }
            return out;
        });
    }

    private void bindEntity(java.sql.PreparedStatement stmt, T entity) throws SQLException {
        int i = 1;
        EntityTypeMapper.bind(stmt, i++, this.model.idType(), this.model.idOf(entity));
        for (EntityModel.Column column : this.model.columns()) {
            Object value = this.model.readColumnValue(entity, column);
            EntityTypeMapper.bind(stmt, i++, column.type(), value);
        }
        for (EntityModel.BlobDef blob : this.model.blobs()) {
            stmt.setBytes(i++, toBlobBytes(entity, blob));
        }
    }

    private T mapRow(ResultSet rs, Set<String> columns) throws Exception {
        T instance = this.model.newEntity();

        Object id = EntityTypeMapper.read(rs, this.model.idColumn().name(), this.model.idType());
        this.model.writeColumnValue(instance, this.model.idColumn(), id);

        for (EntityModel.Column column : this.model.columns()) {
            if (columns != null && !columns.contains(column.name())) continue;
            Object value = EntityTypeMapper.read(rs, column.name(), column.type());
            this.model.writeColumnValue(instance, column, value);
        }

        if (columns == null) {
            for (EntityModel.BlobDef blob : this.model.blobs()) {
                byte[] bytes = rs.getBytes(blob.name());
                applyBlobBytes(instance, blob, bytes);
            }
        }

        return instance;
    }

    /** Merge fresh DB values into an existing instance (same object reference). */
    private void mergeRow(ResultSet rs, T existing, Set<String> columns) throws Exception {
        for (EntityModel.Column column : this.model.columns()) {
            if (columns != null && !columns.contains(column.name())) continue;
            Object value = EntityTypeMapper.read(rs, column.name(), column.type());
            this.model.writeColumnValue(existing, column, value);
        }
        if (columns == null) {
            for (EntityModel.BlobDef blob : this.model.blobs()) {
                byte[] bytes = rs.getBytes(blob.name());
                applyBlobBytes(existing, blob, bytes);
            }
        }
    }

    private byte[] toBlobBytes(T entity, EntityModel.BlobDef blob) {
        Object raw = blob.handle().get(entity);

        if (raw == null) {
            return EMPTY_BYTES;
        }

        if (raw instanceof BinaryStorage<?> storage) {
            byte[] bytes = storage.asBinary();
            return bytes == null ? EMPTY_BYTES : bytes;
        }

        if (blob.type() == byte[].class) {
            byte[] bytes = (byte[]) raw;
            return bytes == null ? EMPTY_BYTES : bytes;
        }

        if (blob.serializer() != null) {
            byte[] bytes = blob.serializer().serialize(raw);
            return bytes == null ? EMPTY_BYTES : bytes;
        }

        throw new IllegalArgumentException("Unsupported @PersistedBlob field type: " + blob.field().getDeclaringClass().getName() + "#" + blob.field().getName());
    }

    private void applyBlobBytes(T entity, EntityModel.BlobDef blob, byte[] bytes) {
        byte[] input = bytes == null ? EMPTY_BYTES : bytes;

        Object current = blob.handle().get(entity);
        if (current instanceof BinaryStorage<?> storage) {
            storage.loadValue(input);
            return;
        }

        if (BinaryStorage.class.isAssignableFrom(blob.type())) {
            try {
                @SuppressWarnings("unchecked")
                BinaryStorage<Object> storage = (BinaryStorage<Object>) (current != null
                        ? current
                        : blob.binaryStorageConstructor().newInstance());
                storage.loadValue(input);
                blob.handle().set(entity, storage);
                return;
            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate BinaryStorage for field " + blob.field().getName(), e);
            }
        }

        if (blob.type() == byte[].class) {
            blob.handle().set(entity, input);
            return;
        }

        if (blob.serializer() != null) {
            Object value = input.length == 0 ? blob.serializer().defaultValue() : blob.serializer().deserialize(input);
            blob.handle().set(entity, value);
            return;
        }

        throw new IllegalArgumentException("No deserialization strategy for blob field " + blob.field().getDeclaringClass().getName() + "#" + blob.field().getName());
    }

    private void loadRelationsBatch(List<T> entities) {
        if (entities.isEmpty() || this.model.relations().isEmpty()) {
            return;
        }

        List<ID> ids = new ArrayList<>(entities.size());
        Map<ID, T> entitiesById = new LinkedHashMap<>();
        for (T entity : entities) {
            ID id = this.model.idOf(entity);
            ids.add(id);
            entitiesById.put(id, entity);
        }

        for (EntityModel.RelationDef relation : this.model.relations()) {
            Map<ID, List<Object>> grouped = loadRelationValues(relation, ids);
            for (Map.Entry<ID, T> entry : entitiesById.entrySet()) {
                assignRelationCollection(entry.getValue(), relation, grouped.getOrDefault(entry.getKey(), List.of()));
            }
        }
    }

    private Map<ID, List<Object>> loadRelationValues(EntityModel.RelationDef relation, List<ID> ids) {
        String sql = selectRelationByIdsSql(relation, ids.size());
        return this.database.resultPreparedStatement(sql, stmt -> {
            int index = 1;
            for (ID id : ids) {
                EntityTypeMapper.bind(stmt, index++, this.model.idType(), id);
            }

            ResultSet rs = stmt.executeQuery();
            Map<ID, List<Object>> grouped = new LinkedHashMap<>();
            while (rs.next()) {
                ID ownerId = this.model.idType().cast(EntityTypeMapper.read(rs, relation.joinColumn(), this.model.idType()));
                Object value = relation.simple()
                        ? EntityTypeMapper.read(rs, relation.valueColumn(), relation.elementType())
                        : mapRelationObject(rs, relation);
                grouped.computeIfAbsent(ownerId, ignored -> new ArrayList<>()).add(value);
            }
            return grouped;
        });
    }

    private Object mapRelationObject(ResultSet rs, EntityModel.RelationDef relation) {
        try {
            Object child = this.model.newRelationElement(relation);
            for (EntityModel.RelationColumn column : relation.objectColumns()) {
                Object value = EntityTypeMapper.read(rs, column.name(), column.type());
                this.model.writeRelationColumnValue(child, column, value);
            }
            return child;
        } catch (SQLException e) {
            throw new RuntimeException("Unable to map relation object for field " + relation.field().getName(), e);
        }
    }

    private void saveRelationsBatch(List<T> entities) {
        if (entities.isEmpty() || this.model.relations().isEmpty()) {
            return;
        }

        List<ID> ids = new ArrayList<>(entities.size());
        for (T entity : entities) {
            ids.add(this.model.idOf(entity));
        }

        for (EntityModel.RelationDef relation : this.model.relations()) {
            deleteRelationsBatch(relation, ids);

            RelationSql sql = relationSql(relation);
            boolean hasAnyValues = entities.stream().anyMatch(entity -> {
                Object raw = this.model.readRelationField(entity, relation);
                return raw instanceof Collection<?> collection && !collection.isEmpty();
            });
            if (!hasAnyValues) {
                continue;
            }

            this.database.prepareClosingStatement(sql.insert(), stmt -> {
                for (T entity : entities) {
                    ID id = this.model.idOf(entity);
                    Object raw = this.model.readRelationField(entity, relation);
                    if (!(raw instanceof Collection<?> collection) || collection.isEmpty()) {
                        continue;
                    }

                    int order = 0;
                    for (Object element : collection) {
                        int i = 1;
                        EntityTypeMapper.bind(stmt, i++, this.model.idType(), id);
                        stmt.setInt(i++, order++);

                        if (relation.simple()) {
                            EntityTypeMapper.bind(stmt, i, relation.elementType(), element);
                        } else {
                            for (EntityModel.RelationColumn column : relation.objectColumns()) {
                                Object value = this.model.readRelationColumnValue(element, column);
                                EntityTypeMapper.bind(stmt, i++, column.type(), value);
                            }
                        }
                        stmt.addBatch();
                    }
                }
                stmt.executeBatch();
            });
        }
    }

    private void deleteRelations(ID id) {
        if (this.model.relations().isEmpty()) {
            return;
        }
        deleteRelationsBatch(List.of(id));
    }

    private void deleteRelationsBatch(List<ID> ids) {
        if (ids.isEmpty()) {
            return;
        }
        for (EntityModel.RelationDef relation : this.model.relations()) {
            deleteRelationsBatch(relation, ids);
        }
    }

    private void deleteRelationsBatch(EntityModel.RelationDef relation, List<ID> ids) {
        if (ids.isEmpty()) {
            return;
        }
        String sql = deleteRelationByIdsSql(relation, ids.size());
        this.database.prepareClosingStatement(sql, stmt -> {
            int index = 1;
            for (ID id : ids) {
                EntityTypeMapper.bind(stmt, index++, this.model.idType(), id);
            }
            stmt.executeUpdate();
        });
    }

    private void assignRelationCollection(T entity, EntityModel.RelationDef relation, List<Object> values) {
        Class<?> type = relation.field().getType();
        if (java.util.Set.class.isAssignableFrom(type)) {
            this.model.writeRelationField(entity, relation, new LinkedHashSet<>(values));
            return;
        }
        if (Collection.class.isAssignableFrom(type)) {
            this.model.writeRelationField(entity, relation, new ArrayList<>(values));
            return;
        }
        this.model.writeRelationField(entity, relation, new ArrayList<>(values));
    }

    private List<T> normalizeEntities(Iterable<T> entities) {
        Map<ID, T> normalized = new LinkedHashMap<>();
        for (T entity : entities) {
            ID id = this.model.idOf(entity);
            if (normalized.containsKey(id)) {
                normalized.remove(id);
            }
            normalized.put(id, entity);
        }
        return new ArrayList<>(normalized.values());
    }

    private SqlDialectStatement buildCreateTable() {
        String mysql = buildCreateTableSql(SqlDriverType.MYSQL);
        String sqlite = buildCreateTableSql(SqlDriverType.SQLITE);
        String postgres = buildCreateTableSql(SqlDriverType.POSTGRESQL);
        return SqlDialectStatement.builder(mysql)
                .mariadb(mysql)
                .sqlite(sqlite)
                .postgresql(postgres)
                .build();
    }

    private List<EntityModel.IndexDef> buildCreateIndexes() {
        return this.model.indexes();
    }

    private Map<String, RelationSql> buildRelationSql() {
        Map<String, RelationSql> sql = new HashMap<>();
        for (EntityModel.RelationDef relation : this.model.relations()) {
            String bulkDeletePrefix = buildRelationBulkDeletePrefix(relation);
            String bulkDeleteSuffix = buildRelationBulkDeleteSuffix(relation);
            String bulkSelectPrefix = buildRelationBulkSelectPrefix(relation);
            String bulkSelectSuffix = buildRelationBulkSelectSuffix(relation);
            sql.put(relationKey(relation), new RelationSql(
                    buildCreateRelationTable(relation),
                    buildInsertRelationSql(relation),
                    buildDeleteRelationSql(relation),
                    bulkDeletePrefix,
                    bulkDeleteSuffix,
                    bulkDeletePrefix + "|" + bulkDeleteSuffix,
                    bulkSelectPrefix,
                    bulkSelectSuffix,
                    bulkSelectPrefix + "|" + bulkSelectSuffix
            ));
        }
        return sql;
    }

    private void createIndexIfMissing(EntityModel.IndexDef index) {
        SqlDialectStatement create = buildCreateIndexStatement(index);

        if (this.driverType == SqlDriverType.MYSQL || this.driverType == SqlDriverType.MARIADB) {
            this.database.openConnection(connection -> {
                if (indexExists(connection, index.name())) {
                    return;
                }
                try (Statement statement = connection.createStatement()) {
                    statement.executeUpdate(create.resolve(this.driverType));
                }
            });
            return;
        }

        this.database.createClosingStatement(statement -> statement.executeUpdate(create.resolve(this.driverType)));
    }

    private SqlDialectStatement buildCreateIndexStatement(EntityModel.IndexDef index) {
        String columns = String.join(", ", index.columns());
        String unique = index.unique() ? "UNIQUE " : "";
        String withoutIfNotExists = "CREATE " + unique + "INDEX " + index.name() + " ON " + this.model.namespace() + " (" + columns + ")";
        String withIfNotExists = "CREATE " + unique + "INDEX IF NOT EXISTS " + index.name() + " ON " + this.model.namespace() + " (" + columns + ")";
        return SqlDialectStatement.builder(withoutIfNotExists)
                .mariadb(withoutIfNotExists)
                .postgresql(withIfNotExists)
                .sqlite(withIfNotExists)
                .build();
    }

    private boolean indexExists(Connection connection, String indexName) throws SQLException {
        try (ResultSet indexes = connection.getMetaData().getIndexInfo(connection.getCatalog(), null, this.model.namespace(), false, false)) {
            while (indexes.next()) {
                String existing = indexes.getString("INDEX_NAME");
                if (existing != null && existing.toLowerCase(Locale.ROOT).equals(indexName.toLowerCase(Locale.ROOT))) {
                    return true;
                }
            }
            return false;
        }
    }

    private SqlDialectStatement buildCreateRelationTable(EntityModel.RelationDef relation) {
        String mysql = buildCreateRelationTableSql(relation, false);
        String sqlite = buildCreateRelationTableSql(relation, true);
        return SqlDialectStatement.builder(mysql).sqlite(sqlite).build();
    }

    private String buildCreateRelationTableSql(EntityModel.RelationDef relation, boolean sqlite) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(relation.table()).append(" (");
        sb.append(relation.joinColumn()).append(' ')
                .append(EntityTypeMapper.sqlType(this.model.idType(), this.model.idColumn().length(), sqlite))
                .append(" NOT NULL, ")
                .append(relation.orderColumn()).append(' ')
                .append(sqlite ? "INTEGER" : "INT")
                .append(" NOT NULL");

        if (relation.simple()) {
            sb.append(", ").append(relation.valueColumn()).append(' ')
                    .append(EntityTypeMapper.sqlType(relation.elementType(), 255, sqlite));
        } else {
            for (EntityModel.RelationColumn c : relation.objectColumns()) {
                sb.append(", ").append(c.name()).append(' ')
                        .append(EntityTypeMapper.sqlType(c.type(), c.length(), sqlite));
            }
        }
        sb.append(')');
        return sb.toString();
    }

    private String buildInsertRelationSql(EntityModel.RelationDef relation) {
        List<String> cols = new ArrayList<>();
        cols.add(relation.joinColumn());
        cols.add(relation.orderColumn());
        if (relation.simple()) {
            cols.add(relation.valueColumn());
        } else {
            relation.objectColumns().forEach(c -> cols.add(c.name()));
        }

        return "INSERT INTO " + relation.table() + " (" + String.join(", ", cols) + ") VALUES (" + placeholders(cols.size()) + ")";
    }

    private String buildDeleteRelationSql(EntityModel.RelationDef relation) {
        return "DELETE FROM " + relation.table() + " WHERE " + relation.joinColumn() + " = ?";
    }

    private String buildRelationBulkDeletePrefix(EntityModel.RelationDef relation) {
        return "DELETE FROM " + relation.table() + " WHERE " + relation.joinColumn() + " IN (";
    }

    private String buildRelationBulkDeleteSuffix(EntityModel.RelationDef relation) {
        return ")";
    }

    private String buildRelationBulkSelectPrefix(EntityModel.RelationDef relation) {
        StringJoiner select = new StringJoiner(", ");
        select.add(relation.joinColumn());
        if (relation.simple()) {
            select.add(relation.valueColumn());
        } else {
            relation.objectColumns().forEach(c -> select.add(c.name()));
        }
        return "SELECT " + select + " FROM " + relation.table() + " WHERE " + relation.joinColumn() + " IN (";
    }

    private String buildRelationBulkSelectSuffix(EntityModel.RelationDef relation) {
        return ") ORDER BY " + relation.joinColumn() + " ASC, " + relation.orderColumn() + " ASC";
    }

    private String buildCreateTableSql(SqlDriverType dialect) {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(this.model.namespace()).append(" (");

        boolean sqlite = dialect == SqlDriverType.SQLITE;
        EntityModel.Column id = this.model.idColumn();
        sb.append(id.name())
                .append(' ')
                .append(EntityTypeMapper.sqlType(id.type(), id.length(), sqlite))
                .append(" PRIMARY KEY");

        for (EntityModel.Column c : this.model.columns()) {
            sb.append(", ").append(c.name())
                    .append(' ').append(EntityTypeMapper.sqlType(c.type(), c.length(), sqlite));
            if (!c.nullable()) sb.append(" NOT NULL");
            if (!c.defaultValue().isBlank()) sb.append(" DEFAULT ").append(c.defaultValue());
        }

        for (EntityModel.BlobDef blob : this.model.blobs()) {
            sb.append(", ").append(blob.name())
                    .append(' ').append(blobSqlType(blob, dialect));
            if (!blob.nullable()) {
                sb.append(" NOT NULL");
            }
        }

        sb.append(')');
        return sb.toString();
    }

    private String blobSqlType(EntityModel.BlobDef blob, SqlDriverType dialect) {
        if (dialect == SqlDriverType.POSTGRESQL) {
            return "BYTEA";
        }
        if (dialect == SqlDriverType.SQLITE) {
            return "BLOB";
        }

        boolean wantsVarbinary = blob.kind() == PersistedBlobKind.VARBINARY
                || (blob.kind() == PersistedBlobKind.AUTO && blob.length() > 0);

        if (wantsVarbinary && blob.length() > 0 && blob.length() <= 65535) {
            return "VARBINARY(" + blob.length() + ")";
        }

        PersistedBlobTier tier = blob.tier();
        return switch (tier) {
            case TINY -> "TINYBLOB";
            case MEDIUM -> "MEDIUMBLOB";
            case LONG -> "LONGBLOB";
            case NORMAL -> "BLOB";
        };
    }

    private SqlDialectStatement buildUpsert() {
        String table = this.model.namespace();
        List<String> cols = new ArrayList<>();
        cols.add(this.model.idColumn().name());
        for (EntityModel.Column c : this.model.columns()) cols.add(c.name());
        for (EntityModel.BlobDef blob : this.model.blobs()) cols.add(blob.name());

        String columnsJoined = String.join(", ", cols);
        String placeholders = placeholders(cols.size());

        List<String> updatable = new ArrayList<>();
        this.model.columns().forEach(c -> updatable.add(c.name()));
        this.model.blobs().forEach(b -> updatable.add(b.name()));

        String mysqlUpdate = updatable.stream()
                .map(name -> name + " = VALUES(" + name + ")")
                .reduce((a, b) -> a + ", " + b)
                .orElse(this.model.idColumn().name() + " = " + this.model.idColumn().name());

        String pgUpdate = updatable.stream()
                .map(name -> name + " = EXCLUDED." + name)
                .reduce((a, b) -> a + ", " + b)
                .orElse(this.model.idColumn().name() + " = " + this.model.idColumn().name());

        String mysql = "INSERT INTO " + table + " (" + columnsJoined + ") VALUES (" + placeholders + ")" +
                " ON DUPLICATE KEY UPDATE " + mysqlUpdate;

        String postgres = "INSERT INTO " + table + " (" + columnsJoined + ") VALUES (" + placeholders + ")" +
                " ON CONFLICT (" + this.model.idColumn().name() + ") DO UPDATE SET " + pgUpdate;

        String sqlite = "INSERT OR REPLACE INTO " + table + " (" + columnsJoined + ") VALUES (" + placeholders + ")";

        return SqlDialectStatement.builder(mysql)
                .postgresql(postgres)
                .sqlite(sqlite)
                .build();
    }

    private String buildSelectProjection() {
        StringJoiner select = new StringJoiner(", ");
        select.add(this.model.idColumn().name());
        this.model.columns().forEach(c -> select.add(c.name()));
        this.model.blobs().forEach(b -> select.add(b.name()));
        return select.toString();
    }

    private String buildSelectById() {
        return "SELECT " + this.selectProjection + " FROM " + this.model.namespace() + " WHERE " + this.model.idColumn().name() + " = ?";
    }

    private String buildSelectAll() {
        return "SELECT " + this.selectProjection + " FROM " + this.model.namespace();
    }

    private String buildDeleteById() {
        return "DELETE FROM " + this.model.namespace() + " WHERE " + this.model.idColumn().name() + " = ?";
    }

    private String selectAllSortedSql(List<SortClause> sorts, int offset, int limit, Set<String> columns) {
        if ((sorts == null || sorts.isEmpty()) && offset <= 0 && limit < 0 && columns == null) {
            return this.selectAll;
        }

        String projectionKey = columns == null ? this.selectProjection : String.join(",", columns);
        String sortSignature = sortSignature(sorts);
        String signature = projectionKey + "|" + sortSignature + "|" + offset + "|" + limit;
        int templateArity = Math.max(0, limit);

        return TEMPLATE_CACHE.getOrCompute(
                "entity-select-all-sorted",
                this.model.namespace(),
                signature,
                templateArity,
                this.driverType,
                ignored -> {
                    String projection;
                    if (columns == null) {
                        projection = this.selectProjection;
                    } else {
                        Set<String> withId = new LinkedHashSet<>();
                        withId.add(this.model.idColumn().name());
                        withId.addAll(columns);
                        projection = String.join(", ", withId);
                    }

                    StringBuilder sql = new StringBuilder("SELECT ").append(projection)
                            .append(" FROM ").append(this.model.namespace());

                    if (sorts != null && !sorts.isEmpty()) {
                        sql.append(" ORDER BY ");
                        StringJoiner orderBy = new StringJoiner(", ");
                        for (SortClause sort : sorts) {
                            EntityModel.Column column = this.model.resolveColumn(sort.field());
                            String direction = sort.direction() == SortDirection.DESC ? "DESC" : "ASC";
                            orderBy.add(column.name() + " " + direction);
                        }
                        sql.append(orderBy);
                    }

                    int effectiveLimit = limit;
                    if (effectiveLimit < 0 && offset > 0) {
                        effectiveLimit = Integer.MAX_VALUE;
                    }
                    if (effectiveLimit >= 0) {
                        sql.append(" LIMIT ").append(effectiveLimit);
                    }
                    if (offset > 0) {
                        sql.append(" OFFSET ").append(offset);
                    }
                    return sql.toString();
                }
        );
    }

    /**
     * Resolves user-facing field/column names (Java field name or SQL column name)
     * to actual SQL column names in this entity's model.
     */
    private Set<String> resolveColumns(Set<String> requested) {
        Set<String> resolved = new LinkedHashSet<>();
        for (String name : requested) {
            try {
                EntityModel.Column column = this.model.resolveColumn(name);
                resolved.add(column.name());
            } catch (IllegalArgumentException ignored) {
                // skip unknown columns gracefully
            }
        }
        return resolved;
    }

    private String sortSignature(List<SortClause> sorts) {
        if (sorts == null || sorts.isEmpty()) {
            return "none";
        }
        StringJoiner signature = new StringJoiner(";");
        for (SortClause sort : sorts) {
            EntityModel.Column column = this.model.resolveColumn(sort.field());
            signature.add(column.name() + ":" + sort.direction().name());
        }
        return signature.toString();
    }

    private String selectByIdsSql(int arity) {
        return TEMPLATE_CACHE.getOrCompute(
                "entity-select-by-ids",
                this.model.namespace(),
                this.selectProjection + "|" + this.model.idColumn().name(),
                arity,
                this.driverType,
                count -> "SELECT " + this.selectProjection + " FROM " + this.model.namespace() +
                        " WHERE " + this.model.idColumn().name() + " IN (" + placeholders(count) + ")"
        );
    }

    private String rankByIdsSql(List<SortClause> sorts, int arity) {
        String sortSignature = sortSignature(sorts);
        String orderBy = buildOrderBy(sorts);
        String signature = this.model.idColumn().name() + "|" + sortSignature;

        return TEMPLATE_CACHE.getOrCompute(
                "entity-rank-by-ids",
                this.model.namespace(),
                signature,
                arity,
                this.driverType,
                count -> {
                    String idColumn = this.model.idColumn().name();
                    return "SELECT " + idColumn + ", rank_value FROM (" +
                            "SELECT " + idColumn + ", ROW_NUMBER() OVER (ORDER BY " + orderBy + ") AS rank_value " +
                            "FROM " + this.model.namespace() +
                            ") ranked WHERE " + idColumn + " IN (" + placeholders(count) + ")";
                }
        );
    }

    private String buildOrderBy(List<SortClause> sorts) {
        if (sorts == null || sorts.isEmpty()) {
            return this.model.idColumn().name() + " ASC";
        }
        StringJoiner orderBy = new StringJoiner(", ");
        for (SortClause sort : sorts) {
            EntityModel.Column column = this.model.resolveColumn(sort.field());
            String direction = sort.direction() == SortDirection.DESC ? "DESC" : "ASC";
            orderBy.add(column.name() + " " + direction);
        }
        return orderBy.toString();
    }

    private String deleteRelationByIdsSql(EntityModel.RelationDef relation, int arity) {
        RelationSql sql = relationSql(relation);
        return TEMPLATE_CACHE.getOrCompute(
                "relation-delete-by-ids",
                relation.table(),
                sql.bulkDeleteSignature(),
                arity,
                this.driverType,
                count -> sql.bulkDeletePrefix() + placeholders(count) + sql.bulkDeleteSuffix()
        );
    }

    private String selectRelationByIdsSql(EntityModel.RelationDef relation, int arity) {
        RelationSql sql = relationSql(relation);
        return TEMPLATE_CACHE.getOrCompute(
                "relation-select-by-ids",
                relation.table(),
                sql.bulkSelectSignature(),
                arity,
                this.driverType,
                count -> sql.bulkSelectPrefix() + placeholders(count) + sql.bulkSelectSuffix()
        );
    }

    private RelationSql relationSql(EntityModel.RelationDef relation) {
        RelationSql sql = this.relationSql.get(relationKey(relation));
        return Objects.requireNonNull(sql, "Missing cached relation SQL for " + relation.field().getName());
    }

    private String relationKey(EntityModel.RelationDef relation) {
        return relation.field().getName();
    }

    private static String placeholders(int count) {
        return String.join(", ", Collections.nCopies(count, "?"));
    }
}
