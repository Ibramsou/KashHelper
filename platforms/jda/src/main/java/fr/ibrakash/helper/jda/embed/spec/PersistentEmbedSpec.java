package fr.ibrakash.helper.jda.embed.spec;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Fully parsed specification of a persistent embed message.
 *
 * <p>A spec is either rendered as a <b>classic embed</b> (when
 * {@link #useV2Components()} is {@code false}) or as a Discord
 * <b>Components V2 Container</b> message (when {@code true}).
 *
 * <p>Components V2 is automatically selected when any of the V2-only
 * fields are populated ({@code textDisplays}, {@code sections},
 * {@code separators}, {@code mediaGalleries}).
 */
public final class PersistentEmbedSpec {

    // ── Classic embed ────────────────────────────────────────────────────────
    private final String title;
    private final List<String> bodyLines;
    private final List<EmbedFieldSpec> fields;
    private final EmbedAuthorSpec author;
    private final EmbedFooterSpec footer;
    private final String imageUrl;
    private final String thumbnailUrl;
    private final String color;

    // ── V2 components ────────────────────────────────────────────────────────
    private final List<String> textDisplays;
    private final List<SectionSpec> sections;
    private final List<MediaGallerySpec> mediaGalleries;
    private final List<SeparatorSpec> separators;
    private final String containerColor;
    private final boolean containerSpoiler;

    // ── Shared (buttons / selects) ───────────────────────────────────────────
    private final List<ActionRowSpec> actionRows;

    private PersistentEmbedSpec(Builder b) {
        this.title = Objects.requireNonNullElse(b.title, "").trim();
        this.bodyLines = List.copyOf(b.bodyLines);
        this.fields = List.copyOf(b.fields);
        this.author = b.author;
        this.footer = b.footer;
        this.imageUrl = b.imageUrl == null ? "" : b.imageUrl.trim();
        this.thumbnailUrl = b.thumbnailUrl == null ? "" : b.thumbnailUrl.trim();
        this.color = b.color == null ? "" : b.color.trim();

        this.textDisplays = List.copyOf(b.textDisplays);
        this.sections = List.copyOf(b.sections);
        this.mediaGalleries = List.copyOf(b.mediaGalleries);
        this.separators = List.copyOf(b.separators);
        this.containerColor = b.containerColor == null ? "" : b.containerColor.trim();
        this.containerSpoiler = b.containerSpoiler;

        this.actionRows = List.copyOf(b.actionRows);
    }

    // ── Classic embed accessors ──────────────────────────────────────────────

    public String title() { return title; }
    public List<String> bodyLines() { return bodyLines; }
    public List<EmbedFieldSpec> fields() { return fields; }
    public EmbedAuthorSpec author() { return author; }
    public EmbedFooterSpec footer() { return footer; }
    public String imageUrl() { return imageUrl; }
    public String thumbnailUrl() { return thumbnailUrl; }
    public String color() { return color; }

    // ── V2 component accessors ───────────────────────────────────────────────

    public List<String> textDisplays() { return textDisplays; }
    public List<SectionSpec> sections() { return sections; }
    public List<MediaGallerySpec> mediaGalleries() { return mediaGalleries; }
    public List<SeparatorSpec> separators() { return separators; }
    public String containerColor() { return containerColor; }
    public boolean containerSpoiler() { return containerSpoiler; }

    // ── Shared accessors ─────────────────────────────────────────────────────

    public List<ActionRowSpec> actionRows() { return actionRows; }

    public List<PersistentButtonSpec> buttons() {
        return this.actionRows.stream()
                .flatMap(row -> row.buttons().stream())
                .toList();
    }

    /**
     * Returns {@code true} if any V2-only component is present, meaning the
     * renderer should use a Container rather than a classic embed.
     */
    public boolean useV2Components() {
        return !textDisplays.isEmpty()
                || !sections.isEmpty()
                || !mediaGalleries.isEmpty()
                || !separators.isEmpty();
    }

    public static Builder builder() { return new Builder(); }

    // ── Builder ──────────────────────────────────────────────────────────────

    public static final class Builder {

        // classic
        private String title;
        private final List<String> bodyLines = new ArrayList<>();
        private final List<EmbedFieldSpec> fields = new ArrayList<>();
        private EmbedAuthorSpec author;
        private EmbedFooterSpec footer;
        private String imageUrl;
        private String thumbnailUrl;
        private String color;

        // v2
        private final List<String> textDisplays = new ArrayList<>();
        private final List<SectionSpec> sections = new ArrayList<>();
        private final List<MediaGallerySpec> mediaGalleries = new ArrayList<>();
        private final List<SeparatorSpec> separators = new ArrayList<>();
        private String containerColor;
        private boolean containerSpoiler;

        // shared
        private final List<ActionRowSpec> actionRows = new ArrayList<>();

        // ── classic ──────────────────────────────────────────────────────────

        public Builder title(String title) { this.title = title; return this; }

        public Builder addBodyLine(String line) {
            if (line != null && !line.isBlank()) this.bodyLines.add(line);
            return this;
        }

        public Builder addField(EmbedFieldSpec field) {
            if (field != null) this.fields.add(field);
            return this;
        }

        public Builder author(EmbedAuthorSpec author) { this.author = author; return this; }
        public Builder footer(EmbedFooterSpec footer) { this.footer = footer; return this; }
        public Builder imageUrl(String url) { this.imageUrl = url; return this; }
        public Builder thumbnailUrl(String url) { this.thumbnailUrl = url; return this; }
        public Builder color(String color) { this.color = color; return this; }

        // ── v2 ───────────────────────────────────────────────────────────────

        public Builder addTextDisplay(String text) {
            if (text != null && !text.isBlank()) this.textDisplays.add(text);
            return this;
        }

        public Builder addSection(SectionSpec section) {
            if (section != null) this.sections.add(section);
            return this;
        }

        public Builder addMediaGallery(MediaGallerySpec gallery) {
            if (gallery != null) this.mediaGalleries.add(gallery);
            return this;
        }

        public Builder addSeparator(SeparatorSpec sep) {
            if (sep != null) this.separators.add(sep);
            return this;
        }

        public Builder containerColor(String color) { this.containerColor = color; return this; }
        public Builder containerSpoiler(boolean spoiler) { this.containerSpoiler = spoiler; return this; }

        // ── shared ───────────────────────────────────────────────────────────

        public Builder addActionRow(ActionRowSpec row) {
            if (row != null) this.actionRows.add(row);
            return this;
        }

        public PersistentEmbedSpec build() { return new PersistentEmbedSpec(this); }
    }
}
