package fr.ibrakash.helper.jda.embed.spec;

public record SeparatorSpec(boolean divider, String spacing) {
    public SeparatorSpec {
        spacing = spacing == null ? "small" : spacing.trim().toLowerCase();
    }
}

