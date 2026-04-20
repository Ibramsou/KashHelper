package fr.ibrakash.helper.jda.embed.spec;

import java.util.List;

public record SectionSpec(
        List<String> textLines,
        String accessoryType,
        String accessoryUrl,
        PersistentButtonSpec accessoryButton
) {
    public SectionSpec {
        textLines = textLines == null ? List.of() : List.copyOf(textLines);
        accessoryType = accessoryType == null ? "" : accessoryType.trim().toLowerCase();
        accessoryUrl = accessoryUrl == null ? "" : accessoryUrl.trim();
    }
}

