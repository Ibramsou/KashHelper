package fr.ibrakash.helper.jda.embed.spec;

import java.util.Objects;

public record PersistentButtonSpec(
        String id,
        String label,
        PersistentButtonStyle style,
        boolean disabled,
        String url
) {
    public PersistentButtonSpec {
        id = id == null ? "" : id.trim();
        label = Objects.requireNonNullElse(label, "Button").trim();
        style = Objects.requireNonNullElse(style, PersistentButtonStyle.SECONDARY);
        url = url == null ? "" : url.trim();
    }
}

