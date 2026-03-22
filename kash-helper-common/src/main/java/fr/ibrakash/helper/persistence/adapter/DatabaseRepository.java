package fr.ibrakash.helper.persistence.adapter;

import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.PersistenceSession;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.persistence.entity.PersistedId;
import fr.ibrakash.helper.persistence.entity.internal.EntityModel;
import fr.ibrakash.helper.persistence.query.SortClause;
import fr.ibrakash.helper.persistence.query.SortDirection;
import fr.ibrakash.helper.persistence.query.SortQuery;
import fr.ibrakash.helper.platform.KashAddon;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Base class for all data repositories.
 *
 * <p>A {@code DatabaseRepository} owns the in-memory caches, the persistence session,
 * and the selected {@link DatabaseAdapter}. Repositories may register custom adapters
 * for one or more backend types, but they can also rely entirely on the built-in
 * default adapters.
 *
 * <h2>Lifecycle</h2>
 * <ol>
 *   <li>Subclass constructor calls {@code super(addon, config)}</li>
 *   <li>Optionally register custom adapters via {@link #registerAdapter}</li>
 *   <li>Optionally call {@link #activateAdapter()} for eager warm-up</li>
 *   <li>Otherwise, the adapter is activated lazily on first persistence use</li>
 *   <li>On disable: call {@link #saveAll()} then {@link #close()}</li>
 * </ol>
 *
 * <h2>R/W helpers</h2>
 * <ul>
 *   <li>{@link #deserializeData(Class, Object)} - load one entity by id</li>
 *   <li>{@link #serializeData(Object, List, List)} - upsert one entity</li>
 *   <li>{@link #serializeBulkData(List, List, List)} - batch upsert</li>
 *   <li>{@link #deserializeBulkData(Class, List)} - batch load by ids</li>
 * </ul>
 *
 * <p>All entity stores are cached internally so {@link #getStore(Class)} always
 * returns the same instance.
 */
public abstract class DatabaseRepository extends PersistenceSession {

    private final Map<DatabaseAdapterType, Function<DatabaseRepository, DatabaseAdapter<DatabaseRepository>>> adapterFactories = new EnumMap<>(DatabaseAdapterType.class);
    private volatile DatabaseAdapter<DatabaseRepository> activeAdapter;
    private volatile boolean adapterActivating;

    // Internal entity-store cache: entityClass -> EntityStore
    private final Map<Class<?>, EntityStore<?, ?>> storeCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, EntityModel<?, ?>> modelCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Map<String, Field>> sortFieldCache = new ConcurrentHashMap<>();

    /**
     * Creates the persistence session and prepares optional adapter registration.
     *
     * <p>Custom adapters can still be registered from the subclass constructor, but
     * repositories are no longer required to register or activate one manually:
     * the default backend adapter is resolved lazily on first persistence use.
     */
    @SuppressWarnings("unchecked")
    protected DatabaseRepository(KashAddon<?> addon, ConfigPersistence config) {
        super(addon, config);
    }

    // -------------------------------------------------------------------------
    // Adapter registration
    // -------------------------------------------------------------------------

    /**
     * Registers an adapter factory for the given backend type.
     * Call this from the subclass constructor before {@link #activateAdapter()}.
     *
     * @param type    backend type
     * @param factory a function that receives {@code this} and constructs the adapter
     * @param <R>     concrete repository type
     */
    @SuppressWarnings("unchecked")
    public <R extends DatabaseRepository> void registerAdapter(DatabaseAdapterType type, Function<R, DatabaseAdapter<R>> factory) {
        this.adapterFactories.put(type, (Function<DatabaseRepository, DatabaseAdapter<DatabaseRepository>>) (Function<?, ?>) factory);
    }

    /**
     * Selects and initialises the adapter matching the active backend.
     *
     * <p>If no custom adapter was registered for the current backend, a built-in
     * default adapter is created automatically. Calling this method is now optional:
     * it only forces early activation (useful for cache warm-up in custom adapters).
     */
    protected final void activateAdapter() {
        ensureAdapterActivated();
    }

    protected final void ensureAdapterActivated() {
        if (this.activeAdapter != null || this.adapterActivating) {
            return;
        }
        synchronized (this) {
            if (this.activeAdapter != null || this.adapterActivating) {
                return;
            }
            this.adapterActivating = true;
            try {
                DatabaseAdapterType adapterType = DatabaseAdapterType.from(this.backendType());
                Function<DatabaseRepository, DatabaseAdapter<DatabaseRepository>> factory = this.adapterFactories.get(adapterType);
                this.activeAdapter = factory != null ? factory.apply(this) : createDefaultAdapter(adapterType);
            } finally {
                this.adapterActivating = false;
            }
        }
    }

    private DatabaseAdapter<DatabaseRepository> createDefaultAdapter(DatabaseAdapterType type) {
        return switch (type) {
            case JSON -> new DefaultJsonAdapter(this);
            case SQL -> new DefaultSqlAdapter(this);
            case MONGO -> new DefaultMongoAdapter(this);
        };
    }

    // -------------------------------------------------------------------------
    // Entity store access
    // -------------------------------------------------------------------------

    /**
     * Returns (or creates) the {@link EntityStore} for the given entity class.
     * The id type is inferred from {@link PersistedId}.
     */
    @SuppressWarnings("unchecked")
    public <T, ID> EntityStore<T, ID> getStore(Class<T> entityClass) {
        ensureAdapterActivated();
        return (EntityStore<T, ID>) storeCache.computeIfAbsent(entityClass, c -> {
            Class<ID> idType = (Class<ID>) inferIdType(c);
            return this.entity(c, idType);
        });
    }

    /**
     * Returns (or creates) the {@link EntityStore} with an explicit id type.
     */
    @SuppressWarnings("unchecked")
    public <T, ID> EntityStore<T, ID> getStore(Class<T> entityClass, Class<ID> idType) {
        ensureAdapterActivated();
        return (EntityStore<T, ID>) storeCache.computeIfAbsent(entityClass, c -> this.entity(c, idType));
    }

    @SuppressWarnings("unchecked")
    protected <T, ID> EntityModel<T, ID> getModel(Class<T> entityClass) {
        return (EntityModel<T, ID>) this.modelCache.computeIfAbsent(entityClass, c -> {
            Class<ID> idType = (Class<ID>) inferIdType(c);
            return EntityModel.from(entityClass, idType);
        });
    }

    // -------------------------------------------------------------------------
    // R/W helpers — single
    // -------------------------------------------------------------------------

    /**
     * Loads one entity from the backend by id.
     *
     * <pre>{@code
     * ExampleData data = this.deserializeData(ExampleData.class, uuid);
     * }</pre>
     *
     * @param entityClass entity type
     * @param id          primary key value
     * @param <T>         entity type
     * @param <ID>        id type
     * @return the entity, or {@code null} if not found
     */
    @SuppressWarnings("unchecked")
    public <T, ID> T deserializeData(Class<T> entityClass, ID id) {
        EntityStore<T, ID> store = getStore(entityClass);
        return store.find(id).orElse(null);
    }

    /**
     * Loads one entity as an {@link Optional}.
     */
    public <T, ID> Optional<T> deserializeOptional(Class<T> entityClass, ID id) {
        EntityStore<T, ID> store = getStore(entityClass);
        return store.find(id);
    }

    /**
     * Upserts one entity to the backend.
     *
     * <p>The {@code keys} and {@code values} parameters exist for documentation /
     * explicitness purposes in the calling code — the actual persistence is driven
     * by the annotated entity fields via {@link EntityStore#save(Object)}.
     *
     * <pre>{@code
     * this.serializeData(data,
     *     List.of(data.getUUID()),
     *     List.of(data.getPoints(), data.getScore())
     * );
     * }</pre>
     *
     * @param entity the entity to save
     * @param keys   identifying values (e.g. primary key) — informational only
     * @param values non-key column values — informational only
     * @param <T>    entity type
     */
    @SuppressWarnings("unchecked")
    public <T> void serializeData(T entity, List<?> keys, List<?> values) {
        EntityStore<T, ?> store = (EntityStore<T, ?>) getStore(entity.getClass());
        store.save(entity);
    }

    // -------------------------------------------------------------------------
    // R/W helpers — bulk
    // -------------------------------------------------------------------------

    /**
     * Batch-upserts a list of entities.
     *
     * <p>{@code keyExtractors} and {@code valueExtractors} are provided for
     * explicitness in the caller and are not used for actual persistence.
     *
     * <pre>{@code
     * this.serializeBulkData(dataList,
     *     List.of(ExampleData::getUUID),
     *     List.of(ExampleData::getPoints, ExampleData::getScore)
     * );
     * }</pre>
     *
     * @param entities       list of entities to save
     * @param keyExtractors  functions that extract key fields — informational
     * @param valueExtractors functions that extract value fields — informational
     * @param <T>            entity type
     */
    @SuppressWarnings("unchecked")
    public <T> void serializeBulkData(List<T> entities,
                                      List<Function<T, ?>> keyExtractors,
                                      List<Function<T, ?>> valueExtractors) {
        if (entities.isEmpty()) return;
        Class<T> entityClass = (Class<T>) entities.get(0).getClass();
        EntityStore<T, ?> store = (EntityStore<T, ?>) getStore(entityClass);
        store.saveAll(entities);
    }

    /**
     * Batch-loads entities by a list of ids.
     *
     * <pre>{@code
     * List<ExampleData> loaded = this.deserializeBulkData(ExampleData.class, uuids);
     * }</pre>
     *
     * @param entityClass entity type
     * @param ids         list of primary key values to fetch
     * @param <T>         entity type
     * @param <ID>        id type
     * @return list of found entities (missing ids are silently skipped)
     */
    public <T, ID> List<T> deserializeBulkData(Class<T> entityClass, List<ID> ids) {
        EntityStore<T, ID> store = getStore(entityClass);
        return store.findAllByIds(ids);
    }

    /**
     * Batch-loads entities by ids — overload matching the {@code ExampleRepository}
     * signature where ids are wrapped inside a single-element outer list.
     */
    public <T, ID> List<T> deserializeBulkData(Class<T> entityClass,
                                                List<List<ID>> idLists,
                                                List<Function<T, ?>> valueExtractors) {
        List<ID> flat = new ArrayList<>();
        for (List<ID> chunk : idLists) flat.addAll(chunk);
        return deserializeBulkData(entityClass, flat);
    }

    // -------------------------------------------------------------------------
    // Sorted reads
    // -------------------------------------------------------------------------

    public <T> SortBuilder<T> sortBuilder(Class<T> entityClass) {
        return new SortBuilder<>(entityClass);
    }

    public <T> RankBuilder<T> rankBuilder(Class<T> entityClass) {
        return new RankBuilder<>(entityClass);
    }

    public <T> RankUpdater<T> rankUpdater(Class<T> entityClass) {
        return new RankUpdater<>(entityClass);
    }

    public <T> List<T> sortDeserializedProfiles(Class<T> entityClass,
                                                String sortField,
                                                SortDirection direction,
                                                int limit) {
        return this.sortBuilder(entityClass)
                .clause(sortField, direction)
                .limit(limit)
                .build();
    }

    public <T> List<T> sortDeserializedProfiles(Class<T> entityClass,
                                                List<SortClause> sorts,
                                                int limit) {
        SortBuilder<T> builder = this.sortBuilder(entityClass).limit(limit);
        for (SortClause sort : sorts) {
            builder.clause(sort.field(), sort.direction());
        }
        return builder.build();
    }

    public <T> List<T> sortDeserializedProfiles(Class<T> entityClass,
                                                Comparator<T> comparator) {
        return this.sortBuilder(entityClass)
                .memoryCache(getStore(entityClass).findAll())
                .comparator(comparator)
                .build();
    }

    public <T> List<T> sortDeserializedProfiles(Class<T> entityClass,
                                                Comparator<T> comparator,
                                                int limit) {
        return this.sortBuilder(entityClass)
                .memoryCache(getStore(entityClass).findAll())
                .comparator(comparator)
                .limit(limit)
                .build();
    }

    public <T> List<T> sortDeserializedProfiles(Class<T> entityClass,
                                                String sortField,
                                                SortDirection direction) {
        return sortDeserializedProfiles(entityClass, sortField, direction, -1);
    }

    public <T> List<T> sortDeserializedProfiles(Class<T> entityClass,
                                                List<SortClause> sorts) {
        return sortDeserializedProfiles(entityClass, sorts, -1);
    }

    public final class SortBuilder<T> {

        private final Class<T> entityClass;
        private final List<SortClause> clauses = new ArrayList<>();
        private int offset = 0;
        private int limit = -1;
        private final Set<String> columns = new LinkedHashSet<>();

        // memory-cache path
        private Collection<T> memoryCacheCollection;
        private Map<?, T> memoryCacheMap;
        private boolean reuseInstances = false;
        private Comparator<T> comparator;

        private SortBuilder(Class<T> entityClass) {
            this.entityClass = entityClass;
        }

        public SortBuilder<T> ascClause(String field) {
            this.clauses.add(SortClause.asc(field));
            return this;
        }

        public SortBuilder<T> descClause(String field) {
            this.clauses.add(SortClause.desc(field));
            return this;
        }

        public SortBuilder<T> clause(String field, SortDirection direction) {
            this.clauses.add(new SortClause(field, direction));
            return this;
        }

        /** Start at row {@code offset} (0-based). */
        public SortBuilder<T> offset(int offset) {
            this.offset = Math.max(0, offset);
            return this;
        }

        public SortBuilder<T> limit(int limit) {
            this.limit = limit;
            return this;
        }

        /** Convenience: page from start index with page size. */
        public SortBuilder<T> window(int offset, int limit) {
            this.offset = Math.max(0, offset);
            this.limit = limit;
            return this;
        }

        /**
         * Restrict SQL SELECT to these columns only (id always included).
         * Ignored when memoryCache is provided.
         */
        public SortBuilder<T> loadColumns(String... fields) {
            this.columns.addAll(List.of(fields));
            return this;
        }

        /** Full column load — explicit no-op, here for readability. */
        public SortBuilder<T> loadFullColumns() {
            this.columns.clear();
            return this;
        }

        /** Sort a pre-loaded Collection without hitting the backend. */
        public SortBuilder<T> memoryCache(Collection<T> cacheValues) {
            this.memoryCacheCollection = cacheValues;
            this.memoryCacheMap = null;
            return this;
        }

        /**
         * Sort using a Map cache.
         *
         * @param cacheMap       the live map (String/ID key → entity)
         * @param reuseInstances if {@code true}, merge fresh DB values into existing
         *                       instances (keeps same object references); if
         *                       {@code false}, replace entries with new instances.
         *                       Ignored when no backend fetch is triggered.
         */
        public <K> SortBuilder<T> memoryCache(Map<K, T> cacheMap, boolean reuseInstances) {
            this.memoryCacheMap = cacheMap;
            this.memoryCacheCollection = null;
            this.reuseInstances = reuseInstances;
            return this;
        }

        /** Convenience — {@code memoryCache(map, false)}. */
        public <K> SortBuilder<T> memoryCache(Map<K, T> cacheMap) {
            return memoryCache(cacheMap, false);
        }

        public SortBuilder<T> comparator(Comparator<T> comparator) {
            this.comparator = comparator;
            return this;
        }

        public List<T> build() {
            return build(Function.identity());
        }

        public <R> List<R> build(Function<T, R> mapper) {
            List<T> sorted = executeSorted();
            if (mapper == Function.identity()) {
                @SuppressWarnings("unchecked")
                List<R> result = (List<R>) sorted;
                return result;
            }
            List<R> out = new ArrayList<>(sorted.size());
            for (T item : sorted) {
                out.add(mapper.apply(item));
            }
            return out;
        }

        private List<T> executeSorted() {
            if (this.memoryCacheCollection != null) {
                return sortInMemory(new ArrayList<>(this.memoryCacheCollection));
            }

            if (this.memoryCacheMap != null) {
                return sortInMemory(new ArrayList<>(this.memoryCacheMap.values()));
            }

            EntityStore<T, ?> store = getStore(this.entityClass);
            SortQuery query = new SortQuery(List.copyOf(this.clauses), this.offset, this.limit, Set.copyOf(this.columns));
            return store.findAllSorted(query);
        }

        private List<T> sortInMemory(List<T> out) {
            if (this.comparator != null) {
                out.sort(this.comparator);
            } else if (!this.clauses.isEmpty()) {
                out.sort(buildMemoryComparator(this.entityClass, this.clauses));
            }
            return applyWindow(out, this.offset, this.limit);
        }
    }

    public final class RankBuilder<T> {

        private final Class<T> entityClass;
        private final List<SortClause> clauses = new ArrayList<>();
        private T data;
        private String rankField;

        private RankBuilder(Class<T> entityClass) {
            this.entityClass = entityClass;
        }

        public RankBuilder<T> data(T data) {
            this.data = data;
            return this;
        }

        public RankBuilder<T> rankField(String rankField) {
            this.rankField = rankField;
            return this;
        }

        public RankBuilder<T> ascClause(String field) {
            this.clauses.add(SortClause.asc(field));
            return this;
        }

        public RankBuilder<T> descClause(String field) {
            this.clauses.add(SortClause.desc(field));
            return this;
        }

        public RankBuilder<T> clause(String field, SortDirection direction) {
            this.clauses.add(new SortClause(field, direction));
            return this;
        }

        public int build() {
            if (this.data == null) {
                throw new IllegalStateException("rankBuilder requires data(entity)");
            }

            EntityStore<T, Object> store = castStore(getStore(this.entityClass));
            EntityModel<T, Object> model = castModel(getModel(this.entityClass));
            Object id = store.idOf(this.data);
            List<SortClause> sorts = resolveSorts(model, this.rankField, this.clauses);
            return store.rankOf(id, sorts);
        }
    }

    public final class RankUpdater<T> {

        private final Class<T> entityClass;
        private final List<T> dataCollection = new ArrayList<>();
        private final Set<String> rankFields = new LinkedHashSet<>();

        private RankUpdater(Class<T> entityClass) {
            this.entityClass = entityClass;
        }

        public RankUpdater<T> data(T data) {
            if (data != null) this.dataCollection.add(data);
            return this;
        }

        public RankUpdater<T> dataCollection(Collection<T> dataCollection) {
            if (dataCollection != null) this.dataCollection.addAll(dataCollection);
            return this;
        }

        public RankUpdater<T> filteringRanks(String... rankNames) {
            if (rankNames != null) this.rankFields.addAll(List.of(rankNames));
            return this;
        }

        public RankUpdater<T> filtering_ranks(String... rankNames) {
            return filteringRanks(rankNames);
        }

        public void update() {
            if (this.dataCollection.isEmpty()) return;

            EntityStore<T, Object> store = castStore(getStore(this.entityClass));
            EntityModel<T, Object> model = castModel(getModel(this.entityClass));
            List<EntityModel.RankDef> defs = resolveRankDefs(model, this.rankFields);
            if (defs.isEmpty()) return;

            List<Object> ids = new ArrayList<>(this.dataCollection.size());
            Map<Object, T> byId = new HashMap<>(this.dataCollection.size());
            for (T data : this.dataCollection) {
                Object id = store.idOf(data);
                ids.add(id);
                byId.put(id, data);
            }

            for (EntityModel.RankDef def : defs) {
                Map<Object, Integer> ranks = store.ranksOf(ids, def.sorts());
                for (Map.Entry<Object, T> entry : byId.entrySet()) {
                    int rankValue = ranks.getOrDefault(entry.getKey(), -1);
                    model.writeRankValue(entry.getValue(), def, rankValue);
                }
            }
        }
    }

    private <T> Comparator<T> buildMemoryComparator(Class<T> entityClass, List<SortClause> clauses) {
        Comparator<T> comparator = null;
        for (SortClause clause : clauses) {
            Field field = resolveSortField(entityClass, clause.field());
            Comparator<T> fieldComparator = (left, right) -> compareFieldValues(field, left, right);
            if (clause.direction() == SortDirection.DESC) {
                fieldComparator = fieldComparator.reversed();
            }
            comparator = comparator == null ? fieldComparator : comparator.thenComparing(fieldComparator);
        }
        return comparator == null ? (left, right) -> 0 : comparator;
    }

    private Field resolveSortField(Class<?> entityClass, String fieldName) {
        Map<String, Field> byName = this.sortFieldCache.computeIfAbsent(entityClass, this::buildSortFieldMap);
        Field resolved = byName.get(fieldName.toLowerCase());
        if (resolved == null) {
            throw new IllegalArgumentException("Unknown memory sort field '" + fieldName + "' for " + entityClass.getSimpleName());
        }
        return resolved;
    }

    private Map<String, Field> buildSortFieldMap(Class<?> entityClass) {
        Map<String, Field> fields = new HashMap<>();
        Class<?> cursor = entityClass;
        while (cursor != null && cursor != Object.class) {
            for (Field field : cursor.getDeclaredFields()) {
                field.setAccessible(true);
                String plain = field.getName().toLowerCase();
                String snake = toSnake(field.getName());
                fields.putIfAbsent(plain, field);
                fields.putIfAbsent(snake, field);
            }
            cursor = cursor.getSuperclass();
        }
        return fields;
    }

    private static String toSnake(String input) {
        StringBuilder out = new StringBuilder(input.length() + 4);
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isUpperCase(c) && i > 0) {
                out.append('_');
            }
            out.append(Character.toLowerCase(c));
        }
        return out.toString();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private int compareFieldValues(Field field, Object left, Object right) {
        Object leftValue;
        Object rightValue;
        try {
            leftValue = field.get(left);
            rightValue = field.get(right);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to read sort field '" + field.getName() + "'", e);
        }

        if (leftValue == rightValue) return 0;
        if (leftValue == null) return 1;
        if (rightValue == null) return -1;

        if (leftValue instanceof Comparable<?> && rightValue instanceof Comparable<?>) {
            return ((Comparable) leftValue).compareTo(rightValue);
        }
        return String.valueOf(leftValue).compareTo(String.valueOf(rightValue));
    }

    private <T> List<T> applyWindow(List<T> values, int offset, int limit) {
        if (offset >= values.size()) {
            return List.of();
        }
        int from = Math.max(0, offset);
        int to = limit < 0 ? values.size() : Math.min(values.size(), from + Math.max(0, limit));
        return new ArrayList<>(values.subList(from, to));
    }

    // -------------------------------------------------------------------------
    // Async helpers
    // -------------------------------------------------------------------------

    /**
     * Wraps a blocking call in a {@link CompletableFuture} running on the
     * SQL thread-pool (if SQL) or the common fork-join pool otherwise.
     */
    public <T> CompletableFuture<T> async(java.util.concurrent.Callable<T> task) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Runnable runner = () -> {
            try {
                future.complete(task.call());
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        };

        if (this.backendType() == PersistenceType.SQL) {
            this.sqlDatabase().getPool().execute(runner);
        } else {
            CompletableFuture.runAsync(runner);
        }
        return future;
    }

    /**
     * Runs a fire-and-forget task asynchronously.
     */
    public CompletableFuture<Void> asyncRun(Runnable task) {
        return async(() -> {
            task.run();
            return null;
        });
    }

    // -------------------------------------------------------------------------
    // Cache utilities
    // -------------------------------------------------------------------------

    /**
     * Populates {@code target} with every record of {@code entityClass} from the backend.
     * Useful inside adapter constructors or {@code onInit}.
     *
     * <pre>{@code
     * // inside SqlAdapter constructor
     * adapter.loadEntireData(repository.getProfileCache(), ProfileRecord.class);
     * }</pre>
     */
    public <K, V> void loadEntireData(Map<K, V> target, Class<V> entityClass) {
        EntityStore<V, K> store = getStore(entityClass);
        store.findAll().forEach(entity -> target.put(store.idOf(entity), entity));
    }

    /**
     * Flushes every entry in {@code source} back to the backend.
     */
    public <V> void flushEntireData(Map<?, V> source, Class<V> entityClass) {
        EntityStore<V, ?> store = getStore(entityClass);
        store.saveAll(source.values());
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    /**
     * Persists all in-memory caches to the backend.
     * Called automatically via {@link #close()}; also safe to call manually for
     * periodic saves.
     */
    public abstract void saveAll();

    @Override
    public void close() {
        this.saveAll();
        super.close();
    }

    /** Compatibility shim for older call-sites. */
    @Deprecated(forRemoval = false)
    public PersistenceSession getSession() {
        return this;
    }

    public DatabaseAdapter<DatabaseRepository> getActiveAdapter() {
        ensureAdapterActivated();
        return activeAdapter;
    }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private static final class DefaultJsonAdapter extends JsonAdapter<DatabaseRepository> {

        private DefaultJsonAdapter(DatabaseRepository repository) {
            super(repository);
        }
    }

    private static final class DefaultSqlAdapter extends SqlAdapter<DatabaseRepository> {

        private DefaultSqlAdapter(DatabaseRepository repository) {
            super(repository);
        }
    }

    private static final class DefaultMongoAdapter extends MongoAdapter<DatabaseRepository> {

        private DefaultMongoAdapter(DatabaseRepository repository) {
            super(repository);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> EntityStore<T, Object> castStore(EntityStore<T, ?> store) {
        return (EntityStore<T, Object>) store;
    }

    @SuppressWarnings("unchecked")
    private <T> EntityModel<T, Object> castModel(EntityModel<T, ?> model) {
        return (EntityModel<T, Object>) model;
    }

    private List<SortClause> resolveSorts(EntityModel<?, ?> model, String rankField, List<SortClause> explicit) {
        if (explicit != null && !explicit.isEmpty()) {
            return List.copyOf(explicit);
        }

        if (rankField != null && !rankField.isBlank()) {
            return model.rank(rankField)
                    .map(EntityModel.RankDef::sorts)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown @PersistedRank field '" + rankField + "' for " + model.entityType().getSimpleName()
                    ));
        }

        if (model.ranks().size() == 1) {
            return model.ranks().get(0).sorts();
        }

        if (!model.ranks().isEmpty()) {
            throw new IllegalStateException("Multiple @PersistedRank fields found on " + model.entityType().getSimpleName() + ". Use rankField(...)");
        }

        return List.of(new SortClause(model.idColumn().name(), SortDirection.ASC));
    }

    private List<EntityModel.RankDef> resolveRankDefs(EntityModel<?, ?> model, Set<String> selectedRankFields) {
        if (model.ranks().isEmpty()) {
            return List.of();
        }
        if (selectedRankFields == null || selectedRankFields.isEmpty()) {
            return model.ranks();
        }

        List<EntityModel.RankDef> out = new ArrayList<>();
        for (String name : selectedRankFields) {
            EntityModel.RankDef def = model.rank(name)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Unknown @PersistedRank field '" + name + "' for " + model.entityType().getSimpleName()
                    ));
            out.add(def);
        }
        return out;
    }

    private static Class<?> inferIdType(Class<?> entityType) {
        for (Field f : entityType.getDeclaredFields()) {
            if (f.getAnnotation(PersistedId.class) != null) return f.getType();
        }
        throw new IllegalArgumentException("No @PersistedId field found in " + entityType.getName());
    }
}

