package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.persistence.entity.NoPersistedBlobSerializer;
import fr.ibrakash.helper.persistence.entity.PersistedBlob;
import fr.ibrakash.helper.persistence.entity.PersistedBlobKind;
import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializer;
import fr.ibrakash.helper.persistence.entity.PersistedBlobTier;
import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import fr.ibrakash.helper.persistence.entity.PersistedEmbedded;
import fr.ibrakash.helper.persistence.entity.PersistedEntity;
import fr.ibrakash.helper.persistence.entity.PersistedDefaultId;
import fr.ibrakash.helper.persistence.entity.PersistedId;
import fr.ibrakash.helper.persistence.entity.PersistedIndex;
import fr.ibrakash.helper.persistence.entity.PersistedIndexes;
import fr.ibrakash.helper.persistence.entity.PersistedRelation;
import fr.ibrakash.helper.persistence.entity.PersistedJson;
import fr.ibrakash.helper.persistence.entity.PersistedJsonMode;
import fr.ibrakash.helper.persistence.entity.PersistedRank;
import fr.ibrakash.helper.persistence.entity.PersistenceLifecycle;
import fr.ibrakash.helper.persistence.query.SortClause;
import fr.ibrakash.helper.persistence.query.SortDirection;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class EntityModel<T, ID> {

    private static final int MAX_INDEX_NAME_LENGTH = 63;

    public record Column(
            Field rootField,
            Field leafField,
            String name,
            Class<?> type,
            boolean nullable,
            String defaultValue,
            int length,
            boolean id,
            VarHandle rootHandle,
            VarHandle leafHandle,
            Constructor<?> rootConstructor
    ) {}

    public record IndexDef(String name, List<String> columns, boolean unique) {}

    public record RelationColumn(Field field, String name, Class<?> type, int length, VarHandle handle) {}

    public record RelationDef(
            Field field,
            VarHandle collectionHandle,
            Constructor<?> elementConstructor,
            String table,
            String joinColumn,
            String valueColumn,
            String orderColumn,
            Class<?> elementType,
            boolean simple,
            List<RelationColumn> objectColumns
    ) {}

    public record BlobDef(
            Field field,
            VarHandle handle,
            String name,
            Class<?> type,
            boolean nullable,
            PersistedBlobKind kind,
            PersistedBlobTier tier,
            int length,
            Class<? extends PersistedBlobSerializer<?>> serializerClass,
            PersistedBlobSerializer<Object> serializer,
            Constructor<?> binaryStorageConstructor
    ) {}

    public record RankDef(
            Field field,
            VarHandle handle,
            String name,
            List<SortClause> sorts,
            boolean loadOnDeserialize
    ) {}

    public record EmbeddedDef(
            Field field,
            VarHandle handle,
            String prefix,
            boolean nullable,
            List<Column> columns
    ) {}

    private final Class<T> entityType;
    private final Class<ID> idType;
    private final String namespace;
    private final Column idColumn;
    private final List<Column> columns;
    private final List<IndexDef> indexes;
    private final List<RelationDef> relations;
    private final List<BlobDef> blobs;
    private final List<EmbeddedDef> embeddeds;
    private final Constructor<T> entityConstructor;
    private final PersistedJsonMode jsonMode;
    private final List<RankDef> ranks;

    private EntityModel(Class<T> entityType,
                        Class<ID> idType,
                        String namespace,
                        Column idColumn,
                        List<Column> columns,
                        List<IndexDef> indexes,
                        List<RelationDef> relations,
                        List<BlobDef> blobs,
                        List<EmbeddedDef> embeddeds,
                        Constructor<T> entityConstructor,
                        PersistedJsonMode jsonMode,
                        List<RankDef> ranks) {
        this.entityType = entityType;
        this.idType = idType;
        this.namespace = namespace;
        this.idColumn = idColumn;
        this.columns = columns;
        this.indexes = indexes;
        this.relations = relations;
        this.blobs = blobs;
        this.embeddeds = embeddeds;
        this.entityConstructor = entityConstructor;
        this.jsonMode = jsonMode;
        this.ranks = ranks;
    }

    public static <T, ID> EntityModel<T, ID> from(Class<T> entityType, Class<ID> idType) {
        Field idField = null;
        String defaultJoinColumn = null;
        List<Column> cols = new ArrayList<>();
        List<RelationDef> relations = new ArrayList<>();
        List<BlobDef> blobs = new ArrayList<>();
        List<RankDef> ranks = new ArrayList<>();
        List<EmbeddedDef> embeddeds = new ArrayList<>();

        for (Field field : entityType.getDeclaredFields()) {
            field.setAccessible(true);

            PersistedId id = field.getAnnotation(PersistedId.class);
            PersistedRelation relation = field.getAnnotation(PersistedRelation.class);
            PersistedEmbedded embedded = field.getAnnotation(PersistedEmbedded.class);
            PersistedColumn col = field.getAnnotation(PersistedColumn.class);
            PersistedBlob blob = field.getAnnotation(PersistedBlob.class);
            PersistedRank rank = field.getAnnotation(PersistedRank.class);

            if (id != null) {
                if (idField != null) {
                    throw new IllegalArgumentException("Multiple @PersistedId fields in " + entityType.getName());
                }
                String name = id.value().isBlank() ? snake(field.getName()) : id.value();
                idField = field;
                defaultJoinColumn = snake(field.getName());
                cols.add(new Column(null, field, name, field.getType(), false, "", 36, true,
                        null, varHandle(field), null));
                continue;
            }

            if (relation != null) {
                continue;
            }

            if (rank != null) {
                int mod = field.getModifiers();
                if (!java.lang.reflect.Modifier.isTransient(mod)) {
                    throw new IllegalArgumentException("@PersistedRank field must be transient: " + entityType.getName() + "#" + field.getName());
                }
                ranks.add(new RankDef(field, varHandle(field), field.getName(), List.of(), rank.load_on_deserialize()));
                continue;
            }

            if (embedded != null) {
                List<Column> embeddedCols = parseEmbedded(field, embedded);
                cols.addAll(embeddedCols);
                String prefix = embedded.prefix().isBlank() ? snake(field.getName()) + "_" : embedded.prefix();
                embeddeds.add(new EmbeddedDef(field, varHandle(field), prefix, embedded.nullable(), embeddedCols));
                continue;
            }

            if (blob != null) {
                blobs.add(parseBlob(field, blob));
                continue;
            }

            if (col == null) {
                int mod = field.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(mod) || java.lang.reflect.Modifier.isTransient(mod) || field.isSynthetic()) {
                    continue;
                }
                cols.add(new Column(null, field, snake(field.getName()), field.getType(), true, "", 255, false,
                        null, varHandle(field), null));
            } else {
                String name = col.value().isBlank() ? snake(field.getName()) : col.value();
                cols.add(new Column(null, field, name, field.getType(), col.nullable(), col.defaultValue(), col.length(), false,
                        null, varHandle(field), null));
            }
        }

        if (idField == null) {
            PersistedDefaultId persistedDefaultId = entityType.getAnnotation(PersistedDefaultId.class);

            if (persistedDefaultId == null) {
                throw new IllegalArgumentException("Missing @PersistedId field in " + entityType.getName());
            }

            String idColumnName = persistedDefaultId.value() == null ? "" : persistedDefaultId.value().trim();
            if (idColumnName.isBlank()) {
                throw new IllegalArgumentException("@PersistedDefaultId value must not be blank in " + entityType.getName());
            }

            Column matchedIdColumn = null;
            for (Column column : cols) {
                if (column.name().equalsIgnoreCase(idColumnName)) {
                    matchedIdColumn = column;
                    break;
                }
            }

            if (matchedIdColumn == null) {
                throw new IllegalArgumentException("@PersistedDefaultId column '" + idColumnName + "' not found in " + entityType.getName());
            }

            List<Column> promoted = new ArrayList<>(cols.size());
            for (Column column : cols) {
                if (column == matchedIdColumn) {
                    promoted.add(new Column(
                            column.rootField(),
                            column.leafField(),
                            column.name(),
                            column.type(),
                            false,
                            column.defaultValue(),
                            column.length(),
                            true,
                            column.rootHandle(),
                            column.leafHandle(),
                            column.rootConstructor()
                    ));
                    continue;
                }
                promoted.add(column);
            }
            cols = promoted;
            defaultJoinColumn = idColumnName;
        }

        for (Field field : entityType.getDeclaredFields()) {
            PersistedRelation relation = field.getAnnotation(PersistedRelation.class);
            if (relation == null) continue;
            field.setAccessible(true);
            relations.add(parseRelation(field, relation, defaultJoinColumn));
        }

        Column idCol = cols.stream().filter(Column::id).findFirst().orElseThrow();
        List<Column> nonId = cols.stream().filter(c -> !c.id()).toList();

        PersistedEntity entityAnn = entityType.getAnnotation(PersistedEntity.class);
        String namespace = entityAnn != null && !entityAnn.value().isBlank()
                ? entityAnn.value()
                : snake(entityType.getSimpleName());

        List<IndexDef> indexes = parseIndexes(entityType);

        List<SortClause> defaultSort = List.of(new SortClause(idCol.name(), SortDirection.ASC));
        List<RankDef> resolvedRanks = new ArrayList<>(ranks.size());
        for (RankDef rank : ranks) {
            PersistedRank ann = rank.field().getAnnotation(PersistedRank.class);
            List<SortClause> parsed = parseSortClauses(ann.sort_columns(), idCol, nonId);
            if (parsed.isEmpty()) parsed = defaultSort;
            resolvedRanks.add(new RankDef(rank.field(), rank.handle(), rank.name(), parsed, rank.loadOnDeserialize()));
        }

        PersistedJson persistedJson = entityType.getAnnotation(PersistedJson.class);
        PersistedJsonMode configured = persistedJson == null ? PersistedJsonMode.AUTO : persistedJson.mode();
        PersistedJsonMode jsonMode = configured == PersistedJsonMode.AUTO
                ? (resolvedRanks.isEmpty() ? PersistedJsonMode.LOAD_ON_DEMAND : PersistedJsonMode.LOAD_ALL)
                : configured;

        return new EntityModel<>(
                entityType,
                idType,
                namespace,
                idCol,
                nonId,
                indexes,
                relations,
                blobs,
                List.copyOf(embeddeds),
                constructor(entityType),
                jsonMode,
                List.copyOf(resolvedRanks)
        );
    }

    private static List<Column> parseEmbedded(Field rootField, PersistedEmbedded embedded) {
        List<Column> out = new ArrayList<>();
        String prefix = embedded.prefix().isBlank() ? snake(rootField.getName()) + "_" : embedded.prefix();
        VarHandle rootHandle = varHandle(rootField);
        Constructor<?> rootConstructor = constructor(rootField.getType());

        for (Field nested : collectInstanceFields(rootField.getType())) {
            nested.setAccessible(true);
            int mod = nested.getModifiers();
            if (java.lang.reflect.Modifier.isStatic(mod) || java.lang.reflect.Modifier.isTransient(mod) || nested.isSynthetic()) {
                continue;
            }

            PersistedColumn nestedCol = nested.getAnnotation(PersistedColumn.class);
            String baseName = nestedCol == null || nestedCol.value().isBlank() ? snake(nested.getName()) : nestedCol.value();
            boolean nullable = nestedCol == null || nestedCol.nullable();
            String defaultValue = nestedCol == null ? "" : nestedCol.defaultValue();
            int length = nestedCol == null ? 255 : nestedCol.length();
            out.add(new Column(rootField, nested, prefix + baseName, nested.getType(), nullable, defaultValue, length, false,
                    rootHandle, varHandle(nested), rootConstructor));
        }

        return out;
    }

    private static List<Field> collectInstanceFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();

        for (Class<?> cursor = type; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            for (Field field : cursor.getDeclaredFields()) {
                if (!seenNames.add(field.getName())) {
                    continue;
                }
                fields.add(field);
            }
        }

        return fields;
    }

    private static RelationDef parseRelation(Field field, PersistedRelation relation, String defaultJoinColumn) {
        if (!(field.getGenericType() instanceof ParameterizedType pt)) {
            throw new IllegalArgumentException("@PersistedRelation field must be parameterized: " + field.getName());
        }

        Type raw = pt.getRawType();
        if (!(raw instanceof Class<?> rawClass) || !java.util.Collection.class.isAssignableFrom(rawClass)) {
            throw new IllegalArgumentException("@PersistedRelation currently supports Collection/List/Set only: " + field.getName());
        }

        Type elementTypeArg = pt.getActualTypeArguments()[0];
        if (!(elementTypeArg instanceof Class<?> elementType)) {
            throw new IllegalArgumentException("Unsupported @PersistedRelation element type on " + field.getName());
        }

        String joinColumn = relation.joinColumn().isBlank()
                ? defaultJoinColumn
                : relation.joinColumn();
        String valueColumn = relation.valueColumn();
        String orderColumn = relation.orderColumn();

        boolean simple = EntityTypeMapper.isSupportedType(elementType);
        if (simple && valueColumn.isBlank()) {
            throw new IllegalArgumentException("@PersistedRelation valueColumn is required for simple element type on field " + field.getName());
        }

        List<RelationColumn> objectColumns = new ArrayList<>();
        Constructor<?> elementConstructor = null;
        if (!simple) {
            if (!valueColumn.isBlank()) {
                throw new IllegalArgumentException("@PersistedRelation valueColumn must be empty for object element type on field " + field.getName());
            }
            String prefix = relation.prefix().isBlank() ? snake(field.getName()) + "_" : relation.prefix();
            elementConstructor = constructor(elementType);
            for (Field nested : collectInstanceFields(elementType)) {
                nested.setAccessible(true);
                int mod = nested.getModifiers();
                if (java.lang.reflect.Modifier.isStatic(mod) || java.lang.reflect.Modifier.isTransient(mod) || nested.isSynthetic()) {
                    continue;
                }
                PersistedColumn nestedCol = nested.getAnnotation(PersistedColumn.class);
                String baseName = nestedCol == null || nestedCol.value().isBlank() ? snake(nested.getName()) : nestedCol.value();
                int length = nestedCol == null ? 255 : nestedCol.length();
                objectColumns.add(new RelationColumn(nested, prefix + baseName, nested.getType(), length, varHandle(nested)));
            }

            if (objectColumns.isEmpty()) {
                throw new IllegalArgumentException("@PersistedRelation object field has no serializable nested fields: " + field.getName());
            }
        }

        return new RelationDef(field, varHandle(field), elementConstructor, relation.table(), joinColumn, valueColumn, orderColumn,
                elementType, simple, objectColumns);
    }

    private static List<IndexDef> parseIndexes(Class<?> type) {
        List<IndexDef> out = new ArrayList<>();

        PersistedIndex one = type.getAnnotation(PersistedIndex.class);
        if (one != null) {
            out.add(toIndex(one, type.getSimpleName()));
        }

        PersistedIndexes many = type.getAnnotation(PersistedIndexes.class);
        if (many != null) {
            for (PersistedIndex index : many.value()) {
                out.add(toIndex(index, type.getSimpleName()));
            }
        }

        return out;
    }

    private static IndexDef toIndex(PersistedIndex ann, String entityName) {
        if (ann.columns().length == 0) {
            throw new IllegalArgumentException("@PersistedIndex on " + entityName + " has no columns");
        }
        String desired = ann.name().isBlank()
                ? "idx_" + snake(entityName) + "_" + String.join("_", ann.columns())
                : ann.name();
        return new IndexDef(shortenIndexName(desired), List.of(ann.columns()), ann.unique());
    }

    private static String shortenIndexName(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.length() <= MAX_INDEX_NAME_LENGTH) {
            return normalized;
        }

        String hash = String.format(Locale.ROOT, "%08x", normalized.hashCode());
        int prefixLength = MAX_INDEX_NAME_LENGTH - 9;
        return normalized.substring(0, prefixLength) + "_" + hash;
    }

    private static String snake(String input) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                out.append('_');
            }
            out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }

    private static VarHandle varHandle(Field field) {
        try {
            return MethodHandles.privateLookupIn(field.getDeclaringClass(), MethodHandles.lookup()).unreflectVarHandle(field);
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException("Unable to create VarHandle for field " + field.getDeclaringClass().getName() + "#" + field.getName(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <V> Constructor<V> constructor(Class<?> type) {
        try {
            Constructor<V> constructor = (Constructor<V>) type.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Type requires a no-arg constructor: " + type.getName(), e);
        }
    }

    private static BlobDef parseBlob(Field field, PersistedBlob blob) {
        String name = blob.value().isBlank() ? snake(field.getName()) : blob.value();

        Class<? extends PersistedBlobSerializer<?>> serializerClass = blob.serializer();
        PersistedBlobSerializer<Object> serializer = PersistedBlobSerializers.resolve(field.getType(), field.getGenericType(), serializerClass);

        boolean hasExplicitSerializer = serializerClass != null && serializerClass != NoPersistedBlobSerializer.class;
        boolean binaryStorage = fr.ibrakash.helper.binary.BinaryStorage.class.isAssignableFrom(field.getType());
        boolean rawBytes = field.getType() == byte[].class;

        if (!binaryStorage && !rawBytes && serializer == null) {
            throw new IllegalArgumentException("@PersistedBlob field requires a serializer or BinaryStorage type: " + field.getName());
        }

        Constructor<?> storageCtor = null;
        if (binaryStorage) {
            storageCtor = constructor(field.getType());
            if (!hasExplicitSerializer && serializer == null) {
                serializer = null; // BinaryStorage handles serialization itself.
            }
        }

        return new BlobDef(
                field,
                varHandle(field),
                name,
                field.getType(),
                blob.nullable(),
                blob.kind(),
                blob.blobTier(),
                blob.length(),
                serializerClass,
                serializer,
                storageCtor
        );
    }

    private static List<SortClause> parseSortClauses(String[] values, Column idColumn, List<Column> columns) {
        if (values == null || values.length == 0) {
            return List.of();
        }
        List<SortClause> out = new ArrayList<>(values.length);
        for (String raw : values) {
            if (raw == null || raw.isBlank()) continue;
            String spec = raw.trim();
            String[] parts = spec.split("\\s+");
            String field = parts[0];
            SortDirection direction = SortDirection.ASC;
            if (parts.length > 1 && parts[1].equalsIgnoreCase("DESC")) {
                direction = SortDirection.DESC;
            }
            String resolved = resolveSortColumnName(field, idColumn, columns);
            out.add(new SortClause(resolved, direction));
        }
        return out;
    }

    private static String resolveSortColumnName(String fieldOrColumn, Column idColumn, List<Column> columns) {
        if (matchesColumn(idColumn, fieldOrColumn)) {
            return idColumn.name();
        }
        for (Column column : columns) {
            if (matchesColumn(column, fieldOrColumn)) {
                return column.name();
            }
        }
        throw new IllegalArgumentException("Unknown rank/sort field '" + fieldOrColumn + "'");
    }

    private static boolean matchesColumn(Column column, String fieldOrColumn) {
        String normalized = fieldOrColumn.toLowerCase(Locale.ROOT);
        if (column.name().equalsIgnoreCase(normalized)) {
            return true;
        }
        return column.leafField().getName().equalsIgnoreCase(normalized);
    }

    public T newEntity() {
        try {
            return this.entityConstructor.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate entity " + this.entityType.getName(), e);
        }
    }

    public Object newRelationElement(RelationDef relation) {
        try {
            return relation.elementConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate relation element for field " + relation.field().getName(), e);
        }
    }

    public ID idOf(T entity) {
        Object value = readColumnValue(entity, this.idColumn);
        return this.idType.cast(value);
    }

    public Column resolveColumn(String fieldOrColumn) {
        if (fieldOrColumn == null || fieldOrColumn.isBlank()) {
            throw new IllegalArgumentException("Sort field cannot be blank");
        }

        if (matchesColumn(this.idColumn, fieldOrColumn)) {
            return this.idColumn;
        }

        Optional<Column> resolved = this.columns.stream()
                .filter(column -> matchesColumn(column, fieldOrColumn))
                .findFirst();
        return resolved.orElseThrow(() -> new IllegalArgumentException(
                "Unknown sortable field '" + fieldOrColumn + "' for " + this.entityType.getSimpleName()
        ));
    }

    public Object readColumnRawValue(T entity, String fieldOrColumn) {
        Column column = resolveColumn(fieldOrColumn);
        return readColumnValue(entity, column);
    }

    public Object readColumnValue(T entity, Column column) {
        if (column.rootHandle() == null) {
            return column.leafHandle().get(entity);
        }
        Object embedded = column.rootHandle().get(entity);
        if (embedded == null) return null;
        return column.leafHandle().get(embedded);
    }

    public void writeColumnValue(T entity, Column column, Object value) {
        if (column.rootHandle() == null) {
            column.leafHandle().set(entity, value);
            return;
        }

        Object embedded = column.rootHandle().get(entity);
        if (embedded == null) {
            try {
                embedded = column.rootConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Unable to instantiate embedded object for field " + column.rootField().getName(), e);
            }
            column.rootHandle().set(entity, embedded);
        }
        column.leafHandle().set(embedded, value);
    }

    public Object readRelationField(T entity, RelationDef relation) {
        return relation.collectionHandle().get(entity);
    }

    public void writeRelationField(T entity, RelationDef relation, Object value) {
        relation.collectionHandle().set(entity, value);
    }

    public Object readRelationColumnValue(Object element, RelationColumn column) {
        return column.handle().get(element);
    }

    public void writeRelationColumnValue(Object element, RelationColumn column, Object value) {
        column.handle().set(element, value);
    }

    public void writeRankValue(T entity, RankDef rank, int value) {
        rank.handle().set(entity, value);
    }

    public List<BlobDef> blobs() {
        return blobs;
    }

    public Class<T> entityType() {
        return entityType;
    }

    public Class<ID> idType() {
        return idType;
    }

    public String namespace() {
        return namespace;
    }

    public Column idColumn() {
        return idColumn;
    }

    public List<Column> columns() {
        return columns;
    }

    public List<IndexDef> indexes() {
        return indexes;
    }


    public List<RelationDef> relations() {
        return relations;
    }

    public List<EmbeddedDef> embeddeds() {
        return embeddeds;
    }

    public PersistedJsonMode jsonMode() {
        return jsonMode;
    }

    public List<RankDef> ranks() {
        return ranks;
    }

    public Optional<RankDef> rank(String name) {
        if (name == null || name.isBlank()) return Optional.empty();
        return this.ranks.stream().filter(r -> r.name().equalsIgnoreCase(name)).findFirst();
    }

    public List<RankDef> ranksMarkedForDeserialize() {
        return this.ranks.stream().filter(RankDef::loadOnDeserialize).toList();
    }

    /**
     * For each nullable embedded, if all its column values are "default",
     * set the embedded field to null on the entity.
     */
    public void nullifyDefaultEmbeddeds(T entity) {
        for (EmbeddedDef embedded : embeddeds) {
            if (!embedded.nullable()) continue;

            Object embeddedObj = embedded.handle().get(entity);
            if (embeddedObj == null) continue;

            boolean allDefault = true;
            for (Column col : embedded.columns()) {
                Object value = col.leafHandle().get(embeddedObj);
                if (!isDefaultValue(value, col)) {
                    allDefault = false;
                    break;
                }
            }

            if (allDefault) {
                embedded.handle().set(entity, null);
            }
        }
    }

    private boolean isDefaultValue(Object value, Column column) {
        if (value == null) {
            return true;
        }

        Class<?> type = column.type();

        // Check if there's an explicit default value in the annotation
        String defaultLiteral = column.defaultValue();
        if (defaultLiteral != null && !defaultLiteral.isEmpty()) {
            Object parsedDefault = parseDefaultForComparison(defaultLiteral, type);
            if (parsedDefault != null) {
                return value.equals(parsedDefault);
            }
        }

        // Check primitive defaults
        if (type == int.class || type == Integer.class) {
            return value.equals(0);
        }
        if (type == long.class || type == Long.class) {
            return value.equals(0L);
        }
        if (type == short.class || type == Short.class) {
            return value.equals((short) 0);
        }
        if (type == byte.class || type == Byte.class) {
            return value.equals((byte) 0);
        }
        if (type == float.class || type == Float.class) {
            return value.equals(0.0f);
        }
        if (type == double.class || type == Double.class) {
            return value.equals(0.0d);
        }
        if (type == boolean.class || type == Boolean.class) {
            return value.equals(false);
        }
        if (type == char.class || type == Character.class) {
            return value.equals('\0');
        }

        // For objects (String, UUID, etc.), null is default, non-null is significant
        return false;
    }

    private Object parseDefaultForComparison(String literal, Class<?> type) {
        String raw = normalizeDefaultLiteralStatic(literal);
        if (raw.isEmpty()) return null;

        try {
            if (type == String.class) {
                return raw;
            }
            if (type == boolean.class || type == Boolean.class) {
                return Boolean.parseBoolean(raw);
            }
            if (type == int.class || type == Integer.class) {
                return Integer.parseInt(raw);
            }
            if (type == long.class || type == Long.class) {
                return Long.parseLong(raw);
            }
            if (type == short.class || type == Short.class) {
                return Short.parseShort(raw);
            }
            if (type == byte.class || type == Byte.class) {
                return Byte.parseByte(raw);
            }
            if (type == float.class || type == Float.class) {
                return Float.parseFloat(raw);
            }
            if (type == double.class || type == Double.class) {
                return Double.parseDouble(raw);
            }
        } catch (RuntimeException ignored) {
            // Can't parse, return null
        }
        return null;
    }

    private static String normalizeDefaultLiteralStatic(String literal) {
        String trimmed = literal == null ? "" : literal.trim();
        if (trimmed.length() >= 2) {
            char first = trimmed.charAt(0);
            char last = trimmed.charAt(trimmed.length() - 1);
            if ((first == '\'' && last == '\'') || (first == '"' && last == '"')) {
                return trimmed.substring(1, trimmed.length() - 1);
            }
        }
        return trimmed;
    }

    /**
     * Fires {@link PersistenceLifecycle#onDeserialized()} on embedded objects
     * first (bottom-up), then on the entity itself if it implements the interface.
     */
    public void fireLifecycle(T entity) {
        if (entity == null) return;

        // Embedded objects first (bottom-up)
        for (EmbeddedDef embedded : embeddeds) {
            Object embeddedObj = embedded.handle().get(entity);
            if (embeddedObj instanceof PersistenceLifecycle lifecycle) {
                lifecycle.onDeserialized();
            }
        }

        // Then the entity itself
        if (entity instanceof PersistenceLifecycle lifecycle) {
            lifecycle.onDeserialized();
        }
    }
}
