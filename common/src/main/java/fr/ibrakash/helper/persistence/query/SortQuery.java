package fr.ibrakash.helper.persistence.query;

import java.util.List;
import java.util.Set;

/**
 * Immutable descriptor passed from {@code SortBuilder.build()} down to the
 * {@link fr.ibrakash.helper.persistence.EntityStore}.
 *
 * <ul>
 *   <li>{@code sorts}   - ordered list of sort clauses (may be empty)</li>
 *   <li>{@code offset}  - start index, {@code 0} = first row</li>
 *   <li>{@code limit}   - max rows to return, {@code -1} = unlimited</li>
 *   <li>{@code columns} - column/field names to SELECT; empty = all columns</li>
 * </ul>
 */
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
