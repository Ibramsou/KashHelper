package fr.ibrakash.helper.jda.embed.spec;

/**
 * Spec for the footer of a classic embed.
 */
public record EmbedFooterSpec(String text, String iconUrl) {
    public EmbedFooterSpec {
        text = text == null ? "" : text.trim();
        iconUrl = iconUrl == null ? "" : iconUrl.trim();
    }
}

