package fr.ibrakash.helper.jda.embed.spec;

public record EmbedAuthorSpec(String name, String iconUrl, String url) {
    public EmbedAuthorSpec {
        name = name == null ? "" : name.trim();
        iconUrl = iconUrl == null ? "" : iconUrl.trim();
        url = url == null ? "" : url.trim();
    }
}

