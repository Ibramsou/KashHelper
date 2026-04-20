package fr.ibrakash.helper.jda.embed.spec;

public record MediaGalleryItemSpec(String url, String description, boolean spoiler) {
    public MediaGalleryItemSpec {
        url = url == null ? "" : url.trim();
        description = description == null ? "" : description.trim();
    }
}

