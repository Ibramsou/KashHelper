package fr.ibrakash.helper.persistence.query;

import java.util.Objects;

public record SortClause(String field, SortDirection direction) {

    public SortClause {
        Objects.requireNonNull(field, "field");
        Objects.requireNonNull(direction, "direction");
        if (field.isBlank()) {
            throw new IllegalArgumentException("field cannot be blank");
        }
    }

    public static SortClause asc(String field) {
        return new SortClause(field, SortDirection.ASC);
    }

    public static SortClause desc(String field) {
        return new SortClause(field, SortDirection.DESC);
    }
}

