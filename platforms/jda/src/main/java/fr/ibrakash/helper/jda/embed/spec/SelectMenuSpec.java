package fr.ibrakash.helper.jda.embed.spec;

import java.util.List;

public record SelectMenuSpec(
        String id,
        String type,
        String placeholder,
        int minValues,
        int maxValues,
        boolean disabled,
        List<SelectOptionSpec> options
) {
    public SelectMenuSpec {
        id = id == null ? "" : id.trim();
        type = type == null ? "string" : type.trim().toLowerCase();
        placeholder = placeholder == null ? "" : placeholder.trim();
        minValues = Math.max(0, minValues);
        maxValues = Math.max(1, maxValues);
        options = options == null ? List.of() : List.copyOf(options);
    }
}

