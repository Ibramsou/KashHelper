package fr.ibrakash.helper.persistence;

import fr.ibrakash.helper.persistence.query.SortClause;
import fr.ibrakash.helper.persistence.query.SortQuery;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

public interface EntityStore<T, ID> {

    Optional<T> find(ID id);

    List<T> findAll();

    default List<T> findAll(List<SortClause> sorts, int limit) {
        List<T> all = this.findAll();
        // default impl has no ordering - override in SQL/JSON stores
        if (limit < 0 || limit >= all.size()) {
            return all;
        }
        return new ArrayList<>(all.subList(0, limit));
    }

    default List<T> findAll(int limit) {
        return this.findAll(List.of(), limit);
    }

    /**
     * Sorted/projected fetch. Implementations that support partial column
     * projection (SQL) will honor {@link SortQuery#columns()};
     * others fall back to a full load.
     */
    default List<T> findAllSorted(SortQuery query) {
        List<T> values = this.findAll(query.sorts(), query.limit() < 0 ? -1 : query.offset() + query.limit());
        if (query.offset() <= 0) {
            return values;
        }
        if (query.offset() >= values.size()) {
            return List.of();
        }
        return new ArrayList<>(values.subList(query.offset(), values.size()));
    }

    default List<T> findAllByIds(List<ID> ids) {
        List<T> result = new ArrayList<>(ids.size());
        for (ID id : ids) {
            find(id).ifPresent(result::add);
        }
        return result;
    }

    default List<T> findAllByIds(List<ID> ids, Set<String> selectedFields) {
        return findAllByIds(ids);
    }

    default void findAllByIds(List<ID> ids, Set<String> selectedFields, Consumer<T> consumer) {
        for (T entity : findAllByIds(ids, selectedFields == null ? Set.of() : selectedFields)) {
            consumer.accept(entity);
        }
    }

    void save(T entity);

    default void save(T entity, Set<String> selectedFields) {
        save(entity);
    }

    void saveAll(Iterable<T> entities);

    default void saveAll(Iterable<T> entities, Set<String> selectedFields) {
        saveAll(entities);
    }

    void delete(ID id);

    /** Returns the id value of the given entity instance. */
    ID idOf(T entity);

    /**
     * Returns 1-based rank for the given id according to the provided sort clauses.
     * Missing ids return {@code -1}.
     */
    default int rankOf(ID id, List<SortClause> sorts) {
        if (id == null) return -1;
        List<T> sorted = this.findAll(sorts == null ? List.of() : sorts, -1);
        for (int i = 0; i < sorted.size(); i++) {
            if (id.equals(idOf(sorted.get(i)))) {
                return i + 1;
            }
        }
        return -1;
    }

    /**
     * Bulk rank lookup (1-based). Missing ids are omitted from the returned map.
     */
    default java.util.Map<ID, Integer> ranksOf(List<ID> ids, List<SortClause> sorts) {
        if (ids == null || ids.isEmpty()) return java.util.Map.of();
        java.util.Set<ID> wanted = new java.util.HashSet<>(ids);
        java.util.Map<ID, Integer> out = new LinkedHashMap<>();
        List<T> sorted = this.findAll(sorts == null ? List.of() : sorts, -1);
        for (int i = 0; i < sorted.size(); i++) {
            ID current = idOf(sorted.get(i));
            if (wanted.contains(current)) {
                out.put(current, i + 1);
                if (out.size() == wanted.size()) break;
            }
        }
        return out;
    }
}
