package fr.ibrakash.helper.jda.embed.spec;

public record EmbedFooterSpec(String text, String iconUrl) {
    public EmbedFooterSpec {
        text = text == null ? "" : text.trim();
        iconUrl = iconUrl == null ? "" : iconUrl.trim();
    }
}

