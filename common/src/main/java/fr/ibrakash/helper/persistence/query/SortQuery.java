package fr.ibrakash.helper.persistence.query;

import java.util.List;
import java.util.Set;

public record SortQuery(List<SortClause> sorts, int offset, int limit, Set<String> columns) {

    public static final SortQuery EMPTY = new SortQuery(List.of(), 0, -1, Set.of());

    public SortQuery {
        sorts = List.copyOf(sorts);
        columns = Set.copyOf(columns);
        if (offset < 0) {
            throw new IllegalArgumentException("offset must be >= 0");
        }
    }

    public boolean hasColumns() {
        return !columns.isEmpty();
    }

    public boolean hasSorts() {
        return !sorts.isEmpty();
    }
}
