package fr.ibrakash.helper.jda.embed.render;

import fr.ibrakash.helper.jda.embed.spec.*;
import fr.ibrakash.helper.jda.text.JdaTextReplacer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.container.Container;
import net.dv8tion.jda.api.components.container.ContainerChildComponent;
import net.dv8tion.jda.api.components.mediagallery.MediaGallery;
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem;
import net.dv8tion.jda.api.components.section.Section;
import net.dv8tion.jda.api.components.separator.Separator;
import net.dv8tion.jda.api.components.selections.EntitySelectMenu;
import net.dv8tion.jda.api.components.selections.SelectOption;
import net.dv8tion.jda.api.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.components.textdisplay.TextDisplay;
import net.dv8tion.jda.api.components.thumbnail.Thumbnail;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder;
import net.dv8tion.jda.api.utils.messages.MessageCreateData;
import net.dv8tion.jda.api.utils.messages.MessageEditBuilder;
import net.dv8tion.jda.api.utils.messages.MessageEditData;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Converts a {@link PersistentEmbedSpec} into JDA message payloads.
 *
 * <ul>
 *   <li>When {@link PersistentEmbedSpec#useV2Components()} is {@code true}, a
 *       Discord <b>Components V2 Container</b> is built and sent as a
 *       top-level component (no classic embed used).</li>
 *   <li>Otherwise a classic {@link net.dv8tion.jda.api.EmbedBuilder} message
 *       is built with fields, author, footer, thumbnail, image and action rows.</li>
 * </ul>
 *
 * <p>Placeholder substitution happens at parse time (see
 * {@link fr.ibrakash.helper.jda.embed.parser.PersistentEmbedParser}); the
 * renderer operates on already-resolved strings.
 */
public class PersistentEmbedRenderer {

    // ────────────────────────────────────────────────────────────────────────
    // Public API
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Renders a spec into a {@link RenderedPersistentEmbed}.
     *
     * @param spec         the parsed embed specification
     * @param customIdPrefix prefix prepended to every button/select custom-id
     * @param placeholders  legacy map of placeholder → value (applied via replacer if non-null)
     */
    public RenderedPersistentEmbed render(
            PersistentEmbedSpec spec,
            String customIdPrefix,
            Map<String, Object> placeholders
    ) {
        // Build a JdaTextReplacer from the placeholders map (mirrors Paper menus approach)
        JdaTextReplacer replacer = buildReplacer(placeholders);

        if (spec.useV2Components()) {
            return renderV2(spec, customIdPrefix, replacer);
        } else {
            return renderClassic(spec, customIdPrefix, replacer);
        }
    }

    public MessageCreateData createData(RenderedPersistentEmbed rendered) {
        MessageCreateBuilder builder = new MessageCreateBuilder();
        if (rendered.embed() != null) {
            builder.addEmbeds(rendered.embed());
            builder.addComponents(rendered.actionRows());
        } else {
            // V2 payloads (Container/Section/TextDisplay/...) require Components V2 mode.
            builder.useComponentsV2(true);
            builder.addComponents(rendered.topLevelComponents());
        }
        return builder.build();
    }

    public MessageEditData editData(RenderedPersistentEmbed rendered) {
        MessageEditBuilder builder = new MessageEditBuilder();
        if (rendered.embed() != null) {
            builder.setEmbeds(rendered.embed());
            builder.setComponents(rendered.actionRows());
        } else {
            // V2 payloads (Container/Section/TextDisplay/...) require Components V2 mode.
            builder.useComponentsV2(true);
            builder.setComponents(rendered.topLevelComponents());
        }
        return builder.build();
    }

    // ────────────────────────────────────────────────────────────────────────
    // Classic embed rendering
    // ────────────────────────────────────────────────────────────────────────

    private RenderedPersistentEmbed renderClassic(
            PersistentEmbedSpec spec,
            String customIdPrefix,
            JdaTextReplacer replacer
    ) {
        EmbedBuilder eb = new EmbedBuilder();

        // Title
        if (!spec.title().isBlank()) {
            eb.setTitle(apply(replacer, spec.title()));
        }

        // Description (body lines)
        if (!spec.bodyLines().isEmpty()) {
            eb.setDescription(String.join("\n",
                    spec.bodyLines().stream().map(l -> apply(replacer, l)).toList()));
        }

        // Fields
        for (EmbedFieldSpec field : spec.fields()) {
            eb.addField(apply(replacer, field.name()), apply(replacer, field.value()), field.inline());
        }

        // Author
        if (spec.author() != null) {
            EmbedAuthorSpec a = spec.author();
            String name = apply(replacer, a.name());
            String icon = apply(replacer, a.iconUrl());
            String url = apply(replacer, a.url());
            eb.setAuthor(name, url.isBlank() ? null : url, icon.isBlank() ? null : icon);
        }

        // Footer
        if (spec.footer() != null) {
            EmbedFooterSpec f = spec.footer();
            String text = apply(replacer, f.text());
            String icon = apply(replacer, f.iconUrl());
            eb.setFooter(text, icon.isBlank() ? null : icon);
        }

        // Image / Thumbnail
        if (!spec.imageUrl().isBlank()) eb.setImage(apply(replacer, spec.imageUrl()));
        if (!spec.thumbnailUrl().isBlank()) eb.setThumbnail(apply(replacer, spec.thumbnailUrl()));

        // Colour
        if (!spec.color().isBlank()) {
            Color color = parseColor(spec.color());
            if (color != null) eb.setColor(color);
        }

        MessageEmbed embed = eb.build();

        // Action rows
        List<ActionRow> rows = buildActionRows(spec, customIdPrefix, replacer);

        return RenderedPersistentEmbed.classic(embed, rows);
    }

    // ────────────────────────────────────────────────────────────────────────
    // Components V2 rendering
    // ────────────────────────────────────────────────────────────────────────

    private RenderedPersistentEmbed renderV2(
            PersistentEmbedSpec spec,
            String customIdPrefix,
            JdaTextReplacer replacer
    ) {
        List<ContainerChildComponent> children = new ArrayList<>();

        // Classic title as a TextDisplay if present
        if (!spec.title().isBlank()) {
            children.add(TextDisplay.of("## " + apply(replacer, spec.title())));
        }

        // Classic body lines as a single TextDisplay
        if (!spec.bodyLines().isEmpty()) {
            String body = String.join("\n",
                    spec.bodyLines().stream().map(l -> apply(replacer, l)).toList());
            children.add(TextDisplay.of(body));
        }

        // Dedicated <text-display> components
        for (String text : spec.textDisplays()) {
            children.add(TextDisplay.of(apply(replacer, text)));
        }

        // Classic fields rendered as text-display lines
        if (!spec.fields().isEmpty()) {
            StringBuilder fieldBlock = new StringBuilder();
            for (EmbedFieldSpec f : spec.fields()) {
                String name = apply(replacer, f.name());
                String value = apply(replacer, f.value());
                if (f.inline()) {
                    fieldBlock.append("**").append(name).append("**: ").append(value).append("  ");
                } else {
                    fieldBlock.append("**").append(name).append("**\n").append(value).append("\n");
                }
            }
            children.add(TextDisplay.of(fieldBlock.toString().strip()));
        }

        // Sections (text + optional accessory)
        for (SectionSpec section : spec.sections()) {
            List<net.dv8tion.jda.api.components.section.SectionContentComponent> contents = new ArrayList<>();
            for (String text : section.textLines()) {
                String resolved = apply(replacer, text).trim();
                if (!resolved.isBlank()) {
                    contents.add(TextDisplay.of(resolved));
                }
            }

            // Never emit blank TextDisplay (JDA rejects blank content).
            if (contents.isEmpty()) {
                continue;
            }

            net.dv8tion.jda.api.components.section.SectionAccessoryComponent accessory = null;

            if ("thumbnail".equalsIgnoreCase(section.accessoryType()) && !section.accessoryUrl().isBlank()) {
                accessory = Thumbnail.fromUrl(apply(replacer, section.accessoryUrl()));
            } else if ("button".equalsIgnoreCase(section.accessoryType()) && section.accessoryButton() != null) {
                accessory = buildJdaButton(section.accessoryButton(), customIdPrefix, replacer);
            }

            if (accessory != null) {
                children.add(Section.of(accessory, contents));
            } else {
                contents.forEach(c -> children.add((ContainerChildComponent) c));
            }
        }

        // Media galleries
        for (MediaGallerySpec gallery : spec.mediaGalleries()) {
            List<MediaGalleryItem> items = new ArrayList<>();
            for (MediaGalleryItemSpec item : gallery.items()) {
                MediaGalleryItem jdaItem = MediaGalleryItem.fromUrl(apply(replacer, item.url()));
                if (!item.description().isBlank()) jdaItem = jdaItem.withDescription(apply(replacer, item.description()));
                if (item.spoiler()) jdaItem = jdaItem.withSpoiler(true);
                items.add(jdaItem);
            }
            if (!items.isEmpty()) children.add(MediaGallery.of(items));
        }

        // Separators
        for (SeparatorSpec sep : spec.separators()) {
            Separator.Spacing spacing = "large".equalsIgnoreCase(sep.spacing())
                    ? Separator.Spacing.LARGE : Separator.Spacing.SMALL;
            children.add(sep.divider()
                    ? Separator.createDivider(spacing)
                    : Separator.createInvisible(spacing));
        }

        // Action rows (buttons and selects)
        for (ActionRowSpec rowSpec : spec.actionRows()) {
            ActionRow row = buildActionRowComponent(rowSpec, customIdPrefix, replacer);
            if (row != null) children.add(row);
        }

        // Build the container
        Container container = buildContainer(spec, children);

        return RenderedPersistentEmbed.v2(List.of(container));
    }

    private Container buildContainer(PersistentEmbedSpec spec, List<ContainerChildComponent> children) {
        Container container = Container.of(children);
        if (!spec.containerColor().isBlank()) {
            Color color = parseColor(spec.containerColor());
            if (color != null) container = container.withAccentColor(color);
        }
        if (spec.containerSpoiler()) {
            container = container.withSpoiler(true);
        }
        return container;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Shared action row building
    // ────────────────────────────────────────────────────────────────────────

    private List<ActionRow> buildActionRows(
            PersistentEmbedSpec spec, String customIdPrefix, JdaTextReplacer replacer
    ) {
        List<ActionRow> rows = new ArrayList<>();
        List<Button> pendingButtons = new ArrayList<>();

        for (ActionRowSpec rowSpec : spec.actionRows()) {
            if (rowSpec.selectMenu() != null) {
                // Flush any pending buttons first
                if (!pendingButtons.isEmpty()) {
                    rows.addAll(partitionButtons(pendingButtons, customIdPrefix, replacer));
                    pendingButtons.clear();
                }
                ActionRow row = buildActionRowComponent(rowSpec, customIdPrefix, replacer);
                if (row != null) rows.add(row);
            } else {
                for (PersistentButtonSpec btn : rowSpec.buttons()) {
                    pendingButtons.add(buildJdaButton(btn, customIdPrefix, replacer));
                    if (pendingButtons.size() == 5) {
                        rows.add(ActionRow.of(pendingButtons));
                        pendingButtons.clear();
                    }
                }
            }
        }

        if (!pendingButtons.isEmpty()) {
            rows.add(ActionRow.of(pendingButtons));
        }
        return rows;
    }

    private ActionRow buildActionRowComponent(
            ActionRowSpec rowSpec, String customIdPrefix, JdaTextReplacer replacer
    ) {
        if (rowSpec.selectMenu() != null) {
            var menuComponent = buildSelectMenuComponent(rowSpec.selectMenu(), customIdPrefix, replacer);
            if (menuComponent != null) return ActionRow.of(menuComponent);
        }
        if (!rowSpec.buttons().isEmpty()) {
            List<Button> buttons = rowSpec.buttons().stream()
                    .map(b -> buildJdaButton(b, customIdPrefix, replacer))
                    .toList();
            return ActionRow.of(buttons);
        }
        return null;
    }

    private List<ActionRow> partitionButtons(
            List<Button> buttons, String customIdPrefix, JdaTextReplacer replacer
    ) {
        List<ActionRow> rows = new ArrayList<>();
        for (int i = 0; i < buttons.size(); i += 5) {
            rows.add(ActionRow.of(buttons.subList(i, Math.min(i + 5, buttons.size()))));
        }
        return rows;
    }

    // ────────────────────────────────────────────────────────────────────────
    // Component builders
    // ────────────────────────────────────────────────────────────────────────

    private Button buildJdaButton(
            PersistentButtonSpec spec, String prefix, JdaTextReplacer replacer
    ) {
        String label = apply(replacer, spec.label()).trim();
        if (label.isBlank()) {
            label = "Button";
        }

        Button button = switch (spec.style()) {
            case PRIMARY   -> Button.primary(prefix + ":" + spec.id(), label);
            case SECONDARY -> Button.secondary(prefix + ":" + spec.id(), label);
            case SUCCESS   -> Button.success(prefix + ":" + spec.id(), label);
            case DANGER    -> Button.danger(prefix + ":" + spec.id(), label);
            case LINK      -> Button.link(spec.url(), label);
        };
        return spec.disabled() ? button.asDisabled() : button;
    }

    private net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent buildSelectMenuComponent(
            SelectMenuSpec spec, String prefix, JdaTextReplacer replacer
    ) {
        String customId = prefix + ":" + spec.id();
        String placeholder = apply(replacer, spec.placeholder());

        return switch (spec.type()) {
            case "string" -> {
                StringSelectMenu.Builder b = StringSelectMenu.create(customId)
                        .setMinValues(spec.minValues())
                        .setMaxValues(spec.maxValues());
                if (!placeholder.isBlank()) b.setPlaceholder(placeholder);

                int added = 0;
                for (SelectOptionSpec opt : spec.options()) {
                    String label = apply(replacer, opt.label()).trim();
                    String value = apply(replacer, opt.value()).trim();
                    if (label.isBlank() || value.isBlank()) {
                        continue;
                    }

                    SelectOption option = SelectOption.of(label, value);
                    if (!opt.description().isBlank()) {
                        option = option.withDescription(apply(replacer, opt.description()));
                    }
                    if (!opt.emoji().isBlank()) {
                        option = option.withEmoji(net.dv8tion.jda.api.entities.emoji.Emoji.fromFormatted(opt.emoji()));
                    }
                    if (opt.isDefault()) {
                        option = option.withDefault(true);
                    }
                    b.addOptions(option);
                    added++;
                }

                // Discord requires at least one option for a string select.
                if (added == 0) {
                    yield null;
                }

                if (spec.disabled()) yield b.build().asDisabled();
                yield b.build();
            }
            case "user" -> {
                EntitySelectMenu.Builder b = EntitySelectMenu.create(customId, EntitySelectMenu.SelectTarget.USER)
                        .setMinValues(spec.minValues()).setMaxValues(spec.maxValues());
                if (!placeholder.isBlank()) b.setPlaceholder(placeholder);
                if (spec.disabled()) yield b.build().asDisabled();
                yield b.build();
            }
            case "role" -> {
                EntitySelectMenu.Builder b = EntitySelectMenu.create(customId, EntitySelectMenu.SelectTarget.ROLE)
                        .setMinValues(spec.minValues()).setMaxValues(spec.maxValues());
                if (!placeholder.isBlank()) b.setPlaceholder(placeholder);
                if (spec.disabled()) yield b.build().asDisabled();
                yield b.build();
            }
            case "channel" -> {
                EntitySelectMenu.Builder b = EntitySelectMenu.create(customId, EntitySelectMenu.SelectTarget.CHANNEL)
                        .setMinValues(spec.minValues()).setMaxValues(spec.maxValues());
                if (!placeholder.isBlank()) b.setPlaceholder(placeholder);
                if (spec.disabled()) yield b.build().asDisabled();
                yield b.build();
            }
            case "mentionable" -> {
                EntitySelectMenu.Builder b = EntitySelectMenu.create(customId,
                        EntitySelectMenu.SelectTarget.USER, EntitySelectMenu.SelectTarget.ROLE)
                        .setMinValues(spec.minValues()).setMaxValues(spec.maxValues());
                if (!placeholder.isBlank()) b.setPlaceholder(placeholder);
                if (spec.disabled()) yield b.build().asDisabled();
                yield b.build();
            }
            default -> null;
        };
    }

    // ────────────────────────────────────────────────────────────────────────
    // Utilities
    // ────────────────────────────────────────────────────────────────────────

    private JdaTextReplacer buildReplacer(Map<String, Object> placeholders) {
        if (placeholders == null || placeholders.isEmpty()) return null;
        JdaTextReplacer replacer = new JdaTextReplacer();
        placeholders.forEach((k, v) -> replacer.add(k, v));
        return replacer;
    }

    private String apply(JdaTextReplacer replacer, String input) {
        if (input == null) return "";
        return replacer == null ? input : replacer.apply(input);
    }

    private Color parseColor(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            // Support #RRGGBB and #RGB
            return Color.decode(raw.startsWith("#") ? raw : "#" + raw);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    // ────────────────────────────────────────────────────────────────────────
    // Rendered result
    // ────────────────────────────────────────────────────────────────────────

    /**
     * Holds the result of rendering a spec.
     *
     * <ul>
     *   <li>Classic: {@link #embed()} is non-null, {@link #actionRows()} has the button rows.</li>
     *   <li>V2:      {@link #embed()} is null, {@link #topLevelComponents()} has the Container.</li>
     * </ul>
     */
    public static final class RenderedPersistentEmbed {

        private final MessageEmbed embed;
        private final List<ActionRow> actionRows;
        private final List<net.dv8tion.jda.api.components.MessageTopLevelComponent> topLevelComponents;

        private RenderedPersistentEmbed(
                MessageEmbed embed,
                List<ActionRow> actionRows,
                List<net.dv8tion.jda.api.components.MessageTopLevelComponent> topLevelComponents
        ) {
            this.embed = embed;
            this.actionRows = actionRows == null ? List.of() : List.copyOf(actionRows);
            this.topLevelComponents = topLevelComponents == null ? List.of() : List.copyOf(topLevelComponents);
        }

        public static RenderedPersistentEmbed classic(MessageEmbed embed, List<ActionRow> rows) {
            return new RenderedPersistentEmbed(embed, rows, null);
        }

        public static RenderedPersistentEmbed v2(
                List<net.dv8tion.jda.api.components.MessageTopLevelComponent> components
        ) {
            return new RenderedPersistentEmbed(null, null, components);
        }

        /** Non-null for classic embeds. */
        public MessageEmbed embed() { return embed; }

        /** Non-empty for classic embeds with buttons. */
        public List<ActionRow> actionRows() { return actionRows; }

        /** Non-empty for V2 component messages. */
        public List<net.dv8tion.jda.api.components.MessageTopLevelComponent> topLevelComponents() {
            return topLevelComponents;
        }
    }
}
