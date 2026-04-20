package fr.ibrakash.helper.jda.embed.spec;

import java.util.List;

public record MediaGallerySpec(List<MediaGalleryItemSpec> items) {
    public MediaGallerySpec {
        items = items == null ? List.of() : List.copyOf(items);
    }
}

