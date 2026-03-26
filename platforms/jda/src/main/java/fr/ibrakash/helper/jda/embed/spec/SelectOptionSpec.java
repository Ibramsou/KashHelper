package fr.ibrakash.helper.jda.embed.spec;

/**
 * Spec for a select menu option.
 */
public record SelectOptionSpec(String value, String label, String description, String emoji, boolean isDefault) {
    public SelectOptionSpec {
        value = value == null ? "" : value.trim();
        label = label == null ? "" : label.trim();
        description = description == null ? "" : description.trim();
        emoji = emoji == null ? "" : emoji.trim();
    }
}

