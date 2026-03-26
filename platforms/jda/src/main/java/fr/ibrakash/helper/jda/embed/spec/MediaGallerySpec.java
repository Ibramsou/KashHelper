package fr.ibrakash.helper.jda.embed.spec;

import java.util.List;

/**
 * Spec for a media gallery component (V2).
 */
public record MediaGallerySpec(List<MediaGalleryItemSpec> items) {
    public MediaGallerySpec {
        items = items == null ? List.of() : List.copyOf(items);
    }
}

