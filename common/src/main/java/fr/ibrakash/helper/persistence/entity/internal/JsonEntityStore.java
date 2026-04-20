package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.persistence.EntityStore;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.entity.PersistedJsonMode;
import fr.ibrakash.helper.persistence.query.SortClause;
import fr.ibrakash.helper.persistence.query.SortDirection;
import fr.ibrakash.helper.persistence.query.SortQuery;
import fr.ibrakash.helper.utils.JsonUtil;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JsonEntityStore<T, ID> implements EntityStore<T, ID> {

    private final EntityModel<T, ID> model;
    private final PersistedJsonMode mode;

    // LOAD_ALL mode
    private final File allFile;
    private final Map<ID, T> allCache = new LinkedHashMap<>();

    // LOAD_ON_DEMAND mode
    private final File perIdFolder;
    private final Map<ID, T> hotCache = new LinkedHashMap<>();

    public JsonEntityStore(PersistenceEngine engine, Class<T> entityType, Class<ID> idType) {
        this.model = EntityModel.from(entityType, idType);
        this.mode = this.model.jsonMode();

        this.allFile = new File(engine.getStorageFolder(), this.model.namespace() + ".json");
        this.perIdFolder = new File(engine.getStorageFolder(), this.model.namespace());
        if (!this.perIdFolder.exists()) {
            this.perIdFolder.mkdirs();
        }

        if (this.mode == PersistedJsonMode.LOAD_ALL) {
            this.loadAll();
        }
    }

    @Override
    public Optional<T> find(ID id) {
        if (id == null) return Optional.empty();

        T loaded;
        if (this.mode == PersistedJsonMode.LOAD_ALL) {
            loaded = this.allCache.get(id);
        } else {
            loaded = this.hotCache.get(id);
            if (loaded == null) {
                File file = fileForId(id);
                if (!file.exists()) return Optional.empty();
                loaded = JsonUtil.readFile(file, this.model.entityType());
                this.hotCache.put(id, loaded);
            }
        }

        if (loaded != null) {
            applyDeserializeRanks(List.of(loaded));
        }
        return Optional.ofNullable(loaded);
    }

    @Override
    public List<T> findAll() {
        List<T> out;
        if (this.mode == PersistedJsonMode.LOAD_ALL) {
            out = new ArrayList<>(this.allCache.values());
        } else {
            out = loadAllFromPerIdFiles();
        }
        applyDeserializeRanks(out);
        return out;
    }

    @Override
    public List<T> findAll(List<SortClause> sorts, int limit) {
        if (this.mode == PersistedJsonMode.LOAD_ON_DEMAND && sorts != null && !sorts.isEmpty()) {
            throw new IllegalStateException("JSON sort requires LOAD_ALL mode. Add @PersistedJson(mode = PersistedJsonMode.LOAD_ALL) on entity " + this.model.entityType().getSimpleName());
        }

        List<T> out = this.findAll();
        if (sorts != null && !sorts.isEmpty()) {
            out.sort(buildComparator(sorts));
        }

        if (limit >= 0 && limit < out.size()) {
            return new ArrayList<>(out.subList(0, limit));
        }
        return out;
    }

    @Override
    public List<T> findAllSorted(SortQuery query) {
        if (this.mode == PersistedJsonMode.LOAD_ON_DEMAND && query.hasSorts()) {
            throw new IllegalStateException("JSON sort requires LOAD_ALL mode. Add @PersistedJson(mode = PersistedJsonMode.LOAD_ALL) on entity " + this.model.entityType().getSimpleName());
        }

        // JSON mode ignores projected columns
        List<T> sorted = this.findAll(query.sorts(), -1);
        int offset = Math.max(0, query.offset());
        if (offset >= sorted.size()) {
            return List.of();
        }
        int to = query.limit() < 0 ? sorted.size() : Math.min(sorted.size(), offset + Math.max(0, query.limit()));
        return new ArrayList<>(sorted.subList(offset, to));
    }

    @Override
    public List<T> findAllByIds(List<ID> ids) {
        if (ids == null || ids.isEmpty()) return List.of();

        List<T> out = new ArrayList<>(ids.size());
        for (ID id : ids) {
            find(id).ifPresent(out::add);
        }
        applyDeserializeRanks(out);
        return out;
    }

    @Override
    public void save(T entity) {
        ID id = this.model.idOf(entity);
        if (this.mode == PersistedJsonMode.LOAD_ALL) {
            this.allCache.put(id, entity);
            flushAll();
            return;
        }

        this.hotCache.put(id, entity);
        JsonUtil.writeFile(fileForId(id), entity);
    }

    @Override
    public void saveAll(Iterable<T> entities) {
        if (entities == null) return;

        if (this.mode == PersistedJsonMode.LOAD_ALL) {
            for (T entity : entities) {
                this.allCache.put(this.model.idOf(entity), entity);
            }
            flushAll();
            return;
        }

        for (T entity : entities) {
            ID id = this.model.idOf(entity);
            this.hotCache.put(id, entity);
            JsonUtil.writeFile(fileForId(id), entity);
        }
    }

    @Override
    public void delete(ID id) {
        if (id == null) return;

        if (this.mode == PersistedJsonMode.LOAD_ALL) {
            this.allCache.remove(id);
            flushAll();
            return;
        }

        this.hotCache.remove(id);
        File file = fileForId(id);
        if (file.exists()) {
            file.delete();
        }
    }

    @Override
    public ID idOf(T entity) {
        return this.model.idOf(entity);
    }

    @Override
    public int rankOf(ID id, List<SortClause> sorts) {
        ensureRankMode();
        return EntityStore.super.rankOf(id, sorts);
    }

    @Override
    public Map<ID, Integer> ranksOf(List<ID> ids, List<SortClause> sorts) {
        ensureRankMode();
        return EntityStore.super.ranksOf(ids, sorts);
    }

    private void ensureRankMode() {
        if (this.mode != PersistedJsonMode.LOAD_ALL) {
            throw new IllegalStateException("JSON rank requires LOAD_ALL mode. Add @PersistedJson(mode = PersistedJsonMode.LOAD_ALL) on entity " + this.model.entityType().getSimpleName());
        }
    }

    private Comparator<T> buildComparator(List<SortClause> sorts) {
        Comparator<T> comparator = null;
        for (SortClause sort : sorts) {
            Comparator<T> clauseComparator = (left, right) -> compareValues(
                    this.model.readColumnRawValue(left, sort.field()),
                    this.model.readColumnRawValue(right, sort.field())
            );
            if (sort.direction() == SortDirection.DESC) {
                clauseComparator = clauseComparator.reversed();
            }
            comparator = comparator == null ? clauseComparator : comparator.thenComparing(clauseComparator);
        }
        return comparator == null ? (left, right) -> 0 : comparator;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private int compareValues(Object left, Object right) {
        if (left == right) return 0;
        if (left == null) return 1;
        if (right == null) return -1;

        Object normalizedLeft = toComparable(left);
        Object normalizedRight = toComparable(right);
        return ((Comparable) normalizedLeft).compareTo(normalizedRight);
    }

    private Object toComparable(Object value) {
        if (value == null || value instanceof Comparable<?>) {
            return value;
        }
        return String.valueOf(value);
    }

    private void loadAll() {
        this.allCache.clear();
        Map<String, T> raw = JsonUtil.readFileMap(this.allFile, HashMap::new, String.class, this.model.entityType());
        raw.forEach((key, value) -> {
            @SuppressWarnings("unchecked")
            ID id = (ID) EntityTypeMapper.parseId(key, this.model.idType());
            this.allCache.put(id, value);
        });
    }

    private void flushAll() {
        Map<String, T> raw = new LinkedHashMap<>();
        this.allCache.forEach((id, value) -> raw.put(String.valueOf(id), value));
        JsonUtil.writeFile(this.allFile, raw);
    }

    private List<T> loadAllFromPerIdFiles() {
        File[] files = this.perIdFolder.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null || files.length == 0) {
            return List.of();
        }

        List<T> out = new ArrayList<>(files.length);
        for (File file : files) {
            T value = JsonUtil.readFile(file, this.model.entityType());
            if (value == null) continue;
            ID id = this.model.idOf(value);
            this.hotCache.put(id, value);
            out.add(value);
        }
        return out;
    }

    private File fileForId(ID id) {
        String encoded = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(String.valueOf(id).getBytes(StandardCharsets.UTF_8));
        return new File(this.perIdFolder, encoded + ".json");
    }

    private void applyDeserializeRanks(List<T> entities) {
        if (entities == null || entities.isEmpty() || this.mode != PersistedJsonMode.LOAD_ALL) {
            return;
        }

        List<EntityModel.RankDef> defs = this.model.ranksMarkedForDeserialize();
        if (defs.isEmpty()) return;

        List<ID> ids = new ArrayList<>(entities.size());
        for (T entity : entities) {
            ids.add(this.model.idOf(entity));
        }

        for (EntityModel.RankDef def : defs) {
            Map<ID, Integer> ranks = this.ranksOf(ids, def.sorts());
            for (T entity : entities) {
                ID id = this.model.idOf(entity);
                this.model.writeRankValue(entity, def, ranks.getOrDefault(id, -1));
            }
        }
    }

    private void postLoad(T entity) {
        this.model.nullifyDefaultEmbeddeds(entity);
        this.model.fireLifecycle(entity);
    }

    private void postLoadAll(List<T> entities) {
        for (T entity : entities) {
            postLoad(entity);
        }
    }
}
