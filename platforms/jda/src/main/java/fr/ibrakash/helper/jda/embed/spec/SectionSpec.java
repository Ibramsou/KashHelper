package fr.ibrakash.helper.jda.embed.spec;

import java.util.List;

/**
 * Spec for a V2 Section component (text content + optional accessory).
 *
 * <p>The accessory can be a {@code "thumbnail"} (URL-based) or a {@code "button"}.
 * When {@code accessoryType} is {@code "thumbnail"}, use {@code accessoryUrl}.
 * When {@code accessoryType} is {@code "button"}, use the {@code accessoryButton} field.
 */
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

