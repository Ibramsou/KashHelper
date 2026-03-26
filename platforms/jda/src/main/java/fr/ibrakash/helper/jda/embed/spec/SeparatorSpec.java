package fr.ibrakash.helper.jda.embed.spec;

/**
 * Spec for a separator component (V2).
 */
public record SeparatorSpec(boolean divider, String spacing) {
    public SeparatorSpec {
        spacing = spacing == null ? "small" : spacing.trim().toLowerCase();
    }
}

