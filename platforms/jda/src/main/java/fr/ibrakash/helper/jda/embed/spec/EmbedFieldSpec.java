package fr.ibrakash.helper.jda.embed.spec;

public record EmbedFieldSpec(String name, String value, boolean inline) {
    public EmbedFieldSpec {
        name = name == null ? "\u200B" : name.trim();
        value = value == null ? "\u200B" : value.trim();
    }
}

