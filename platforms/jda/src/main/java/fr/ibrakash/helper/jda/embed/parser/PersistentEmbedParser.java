package fr.ibrakash.helper.jda.embed.parser;

import fr.ibrakash.helper.jda.embed.spec.*;
import fr.ibrakash.helper.jda.text.JdaTextReplacer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses pseudo-XML embed lines into {@link PersistentEmbedSpec}.
 */
public class PersistentEmbedParser {

    private static final Pattern TAG_LINE = Pattern.compile(
            "^<(?<tag>[a-zA-Z0-9_-]+)(?<attrs>[^>]*)>(?<content>.*?)</\\1>\\s*$");
    private static final Pattern OPEN_TAG = Pattern.compile(
            "^<(?<tag>[a-zA-Z0-9_-]+)(?<attrs>[^/>]*)>\\s*$");
    private static final Pattern CLOSE_TAG = Pattern.compile(
            "^</(?<tag>[a-zA-Z0-9_-]+)>\\s*$");
    private static final Pattern SELF_CLOSE = Pattern.compile(
            "^<(?<tag>[a-zA-Z0-9_-]+)(?<attrs>[^>]*?)/>\\s*$");
    private static final Pattern ATTR_PATTERN = Pattern.compile(
            "([a-zA-Z0-9_-]+)=['\"]([^'\"]*)['\"]");

    public PersistentEmbedSpec parse(List<String> rawLines) {
        return parse(rawLines, null);
    }

    public PersistentEmbedSpec parse(List<String> rawLines, JdaTextReplacer replacer) {
        if (rawLines == null || rawLines.isEmpty()) {
            throw new IllegalArgumentException("Embed configuration is empty");
        }

        List<String> lines = preprocess(rawLines);
        PersistentEmbedSpec.Builder builder = PersistentEmbedSpec.builder();
        int[] idx = {0};

        if (idx[0] < lines.size()) {
            String first = lines.get(idx[0]);
            Matcher open = OPEN_TAG.matcher(first);
            Matcher selfClose = SELF_CLOSE.matcher(first);
            if (open.matches() && "container".equalsIgnoreCase(open.group("tag"))) {
                Map<String, String> attrs = parseAttrs(open.group("attrs"));
                if (attrs.containsKey("color")) builder.containerColor(attrs.get("color"));
                if ("true".equalsIgnoreCase(attrs.getOrDefault("spoiler", "false"))) builder.containerSpoiler(true);
                idx[0]++;
            } else if (selfClose.matches() && "container".equalsIgnoreCase(selfClose.group("tag"))) {
                idx[0]++;
            }
        }

        while (idx[0] < lines.size()) {
            String line = lines.get(idx[0]);

            Matcher close = CLOSE_TAG.matcher(line);
            if (close.matches() && "container".equalsIgnoreCase(close.group("tag"))) {
                idx[0]++;
                break;
            }

            Matcher tagLine = TAG_LINE.matcher(line);
            if (tagLine.matches()) {
                parseSingleLineTag(
                        builder,
                        tagLine.group("tag").toLowerCase(Locale.ROOT),
                        apply(replacer, tagLine.group("content").trim()),
                        parseAttrs(tagLine.group("attrs"))
                );
                idx[0]++;
                continue;
            }

            Matcher selfClose = SELF_CLOSE.matcher(line);
            if (selfClose.matches()) {
                parseSelfClosingTag(
                        builder,
                        selfClose.group("tag").toLowerCase(Locale.ROOT),
                        parseAttrs(selfClose.group("attrs")),
                        replacer
                );
                idx[0]++;
                continue;
            }

            Matcher open = OPEN_TAG.matcher(line);
            if (open.matches()) {
                idx[0]++;
                parseBlock(
                        builder,
                        open.group("tag").toLowerCase(Locale.ROOT),
                        parseAttrs(open.group("attrs")),
                        lines,
                        idx,
                        replacer
                );
                continue;
            }

            idx[0]++;
        }

        return builder.build();
    }

    private void parseSingleLineTag(PersistentEmbedSpec.Builder builder, String tag, String content, Map<String, String> attrs) {
        switch (tag) {
            case "text" -> {
                if ("title".equalsIgnoreCase(attrs.get("kind"))) builder.title(content);
                else builder.addBodyLine(content);
            }
            case "text-display" -> builder.addTextDisplay(content);
            case "button" -> builder.addActionRow(new ActionRowSpec(List.of(buildButton(attrs, content)), null));
            case "image" -> builder.imageUrl(attrs.getOrDefault("url", content));
            case "thumbnail" -> builder.thumbnailUrl(attrs.getOrDefault("url", content));
            case "color" -> builder.color(content);
            case "field" -> {
                String name = attrs.getOrDefault("name", "Field");
                boolean inline = Boolean.parseBoolean(attrs.getOrDefault("inline", "false"));
                builder.addField(new EmbedFieldSpec(name, content, inline));
            }
        }
    }

    private void parseSelfClosingTag(PersistentEmbedSpec.Builder builder, String tag, Map<String, String> attrs, JdaTextReplacer replacer) {
        switch (tag) {
            case "separator" -> {
                boolean divider = !"false".equalsIgnoreCase(attrs.getOrDefault("divider", "true"));
                builder.addSeparator(new SeparatorSpec(divider, attrs.getOrDefault("spacing", "small")));
            }
            case "author" -> {
                String name = apply(replacer, attrs.getOrDefault("name", ""));
                if (!name.isBlank()) {
                    builder.author(new EmbedAuthorSpec(
                            name,
                            apply(replacer, attrs.getOrDefault("icon", "")),
                            apply(replacer, attrs.getOrDefault("url", ""))
                    ));
                }
            }
            case "thumbnail" -> builder.thumbnailUrl(apply(replacer, attrs.getOrDefault("url", "")));
            case "image" -> builder.imageUrl(apply(replacer, attrs.getOrDefault("url", "")));
            case "footer" -> {
                String text = apply(replacer, attrs.getOrDefault("text", ""));
                if (!text.isBlank()) {
                    builder.footer(new EmbedFooterSpec(text, apply(replacer, attrs.getOrDefault("icon", ""))));
                }
            }
            case "color" -> builder.color(attrs.getOrDefault("value", ""));
        }
    }

    private void parseBlock(
            PersistentEmbedSpec.Builder builder,
            String tag,
            Map<String, String> attrs,
            List<String> lines,
            int[] idx,
            JdaTextReplacer replacer
    ) {
        switch (tag) {
            case "section" -> parseSection(builder, attrs, lines, idx, replacer);
            case "actions" -> parseActions(builder, lines, idx, replacer);
            case "media-gallery" -> parseMediaGallery(builder, lines, idx, replacer);
            case "author" -> parseAuthorBlock(builder, lines, idx, replacer);
            case "footer" -> parseFooterBlock(builder, lines, idx, replacer);
            default -> skipBlock(tag, lines, idx);
        }
    }

    private void parseSection(
            PersistentEmbedSpec.Builder builder,
            Map<String, String> attrs,
            List<String> lines,
            int[] idx,
            JdaTextReplacer replacer
    ) {
        String accessoryType = attrs.getOrDefault("accessory-type", "");
        String accessoryUrl = apply(replacer, attrs.getOrDefault("accessory-url", ""));
        PersistentButtonSpec accessoryButton = null;

        if (attrs.containsKey("accessory-id")) {
            PersistentButtonStyle style = PersistentButtonStyle.fromRaw(attrs.getOrDefault("accessory-style", "secondary"));
            if (style == PersistentButtonStyle.LINK && accessoryUrl.isBlank()) {
                style = PersistentButtonStyle.SECONDARY;
            }
            accessoryButton = new PersistentButtonSpec(
                    attrs.get("accessory-id"),
                    apply(replacer, attrs.getOrDefault("accessory-label", "Button")),
                    style,
                    false,
                    accessoryUrl
            );
        }

        List<String> textLines = new ArrayList<>();
        boolean v2 = !accessoryType.isBlank();

        while (idx[0] < lines.size()) {
            String line = lines.get(idx[0]);
            Matcher close = CLOSE_TAG.matcher(line);
            if (close.matches() && "section".equalsIgnoreCase(close.group("tag"))) {
                idx[0]++;
                break;
            }

            String lower = line.toLowerCase(Locale.ROOT);
            if (lower.startsWith("<text-display>")) {
                String parsed = parseTextDisplayBlock(lines, idx, replacer);
                if (!parsed.isBlank()) {
                    textLines.add(parsed);
                    v2 = true;
                }
                continue;
            }

            Matcher tagLine = TAG_LINE.matcher(line);
            if (tagLine.matches()) {
                String childTag = tagLine.group("tag").toLowerCase(Locale.ROOT);
                String content = apply(replacer, tagLine.group("content").trim());
                Map<String, String> childAttrs = parseAttrs(tagLine.group("attrs"));
                if ("text-display".equals(childTag)) {
                    if (!content.isBlank()) {
                        textLines.add(content);
                        v2 = true;
                    }
                } else if ("text".equals(childTag)) {
                    if ("title".equalsIgnoreCase(childAttrs.get("kind"))) builder.title(content);
                    else builder.addBodyLine(content);
                }
            }
            idx[0]++;
        }

        if (v2) {
            builder.addSection(new SectionSpec(textLines, accessoryType, accessoryUrl, accessoryButton));
        }
    }

    private String parseTextDisplayBlock(List<String> lines, int[] idx, JdaTextReplacer replacer) {
        String line = lines.get(idx[0]);
        StringBuilder out = new StringBuilder();

        String afterOpen = line.substring("<text-display>".length());
        int inlineClose = afterOpen.toLowerCase(Locale.ROOT).indexOf("</text-display>");
        if (inlineClose >= 0) {
            String inline = afterOpen.substring(0, inlineClose).trim();
            idx[0]++;
            return apply(replacer, inline);
        }

        if (!afterOpen.isBlank()) {
            out.append(afterOpen.strip());
        }

        idx[0]++;
        while (idx[0] < lines.size()) {
            String inner = lines.get(idx[0]);
            String lowerInner = inner.toLowerCase(Locale.ROOT);
            int endAt = lowerInner.indexOf("</text-display>");

            if (endAt >= 0) {
                String before = inner.substring(0, endAt).strip();
                if (!before.isBlank()) {
                    if (out.length() > 0) out.append("\n");
                    out.append(before);
                }
                idx[0]++;
                break;
            }

            if (!inner.isBlank()) {
                if (out.length() > 0) out.append("\n");
                out.append(inner.strip());
            }
            idx[0]++;
        }

        return apply(replacer, out.toString().trim());
    }

    private void parseActions(PersistentEmbedSpec.Builder builder, List<String> lines, int[] idx, JdaTextReplacer replacer) {
        List<PersistentButtonSpec> buttons = new ArrayList<>();
        SelectMenuSpec selectMenu = null;

        while (idx[0] < lines.size()) {
            String line = lines.get(idx[0]);
            Matcher close = CLOSE_TAG.matcher(line);
            if (close.matches() && "actions".equalsIgnoreCase(close.group("tag"))) {
                idx[0]++;
                break;
            }

            Matcher tagLine = TAG_LINE.matcher(line);
            Matcher openTag = OPEN_TAG.matcher(line);
            if (tagLine.matches()) {
                String childTag = tagLine.group("tag").toLowerCase(Locale.ROOT);
                if ("button".equals(childTag)) {
                    buttons.add(buildButton(parseAttrs(tagLine.group("attrs")), apply(replacer, tagLine.group("content").trim())));
                }
            } else if (openTag.matches()) {
                String childTag = openTag.group("tag").toLowerCase(Locale.ROOT);
                Map<String, String> childAttrs = parseAttrs(openTag.group("attrs"));
                idx[0]++;
                if (childTag.endsWith("-select") || "string-select".equals(childTag)) {
                    selectMenu = parseSelectMenu(childTag, childAttrs, lines, idx, replacer);
                } else {
                    skipBlock(childTag, lines, idx);
                }
                continue;
            }
            idx[0]++;
        }

        if (!buttons.isEmpty() || selectMenu != null) {
            builder.addActionRow(new ActionRowSpec(buttons, selectMenu));
        }
    }

    private SelectMenuSpec parseSelectMenu(
            String tag,
            Map<String, String> attrs,
            List<String> lines,
            int[] idx,
            JdaTextReplacer replacer
    ) {
        String id = attrs.getOrDefault("id", "");
        String type = tag.replace("-select", "");
        if (type.isBlank() || "string".equals(type)) type = "string";

        String placeholder = apply(replacer, attrs.getOrDefault("placeholder", ""));
        int minValues = parseInt(attrs.getOrDefault("min", "0"));
        int maxValues = parseInt(attrs.getOrDefault("max", "1"));
        boolean disabled = "true".equalsIgnoreCase(attrs.getOrDefault("disabled", "false"));

        List<SelectOptionSpec> options = new ArrayList<>();

        while (idx[0] < lines.size()) {
            String line = lines.get(idx[0]);
            Matcher close = CLOSE_TAG.matcher(line);
            if (close.matches() && (tag.equalsIgnoreCase(close.group("tag")) || close.group("tag").endsWith("-select"))) {
                idx[0]++;
                break;
            }

            Matcher tagLine = TAG_LINE.matcher(line);
            Matcher selfClose = SELF_CLOSE.matcher(line);

            if (tagLine.matches() && "option".equalsIgnoreCase(tagLine.group("tag"))) {
                Map<String, String> optionAttrs = parseAttrs(tagLine.group("attrs"));
                String rawContent = tagLine.group("content").trim();
                String label = apply(replacer, !rawContent.isBlank()
                        ? rawContent
                        : optionAttrs.getOrDefault("label", optionAttrs.getOrDefault("value", "")));
                String value = apply(replacer, optionAttrs.getOrDefault("value", label));
                String description = apply(replacer, optionAttrs.getOrDefault("description", ""));
                String emoji = optionAttrs.getOrDefault("emoji", "");
                boolean isDefault = "true".equalsIgnoreCase(optionAttrs.getOrDefault("default", "false"));
                if (!label.isBlank() && !value.isBlank()) {
                    options.add(new SelectOptionSpec(value, label, description, emoji, isDefault));
                }
            } else if (selfClose.matches() && "option".equalsIgnoreCase(selfClose.group("tag"))) {
                Map<String, String> optionAttrs = parseAttrs(selfClose.group("attrs"));
                String label = apply(replacer, optionAttrs.getOrDefault("label", optionAttrs.getOrDefault("value", "")));
                String value = apply(replacer, optionAttrs.getOrDefault("value", label));
                String description = apply(replacer, optionAttrs.getOrDefault("description", ""));
                String emoji = optionAttrs.getOrDefault("emoji", "");
                boolean isDefault = "true".equalsIgnoreCase(optionAttrs.getOrDefault("default", "false"));
                if (!label.isBlank() && !value.isBlank()) {
                    options.add(new SelectOptionSpec(value, label, description, emoji, isDefault));
                }
            }
            idx[0]++;
        }

        return new SelectMenuSpec(id, type, placeholder, minValues, maxValues, disabled, options);
    }

    private void parseMediaGallery(PersistentEmbedSpec.Builder builder, List<String> lines, int[] idx, JdaTextReplacer replacer) {
        List<MediaGalleryItemSpec> items = new ArrayList<>();

        while (idx[0] < lines.size()) {
            String line = lines.get(idx[0]);
            Matcher close = CLOSE_TAG.matcher(line);
            if (close.matches() && "media-gallery".equalsIgnoreCase(close.group("tag"))) {
                idx[0]++;
                break;
            }

            Matcher selfClose = SELF_CLOSE.matcher(line);
            Matcher tagLine = TAG_LINE.matcher(line);
            Map<String, String> attrs = null;
            String content = "";

            if (selfClose.matches() && "media".equalsIgnoreCase(selfClose.group("tag"))) {
                attrs = parseAttrs(selfClose.group("attrs"));
            } else if (tagLine.matches() && "media".equalsIgnoreCase(tagLine.group("tag"))) {
                attrs = parseAttrs(tagLine.group("attrs"));
                content = apply(replacer, tagLine.group("content").trim());
            }

            if (attrs != null) {
                String url = apply(replacer, attrs.getOrDefault("url", content));
                String description = apply(replacer, attrs.getOrDefault("description", ""));
                boolean spoiler = "true".equalsIgnoreCase(attrs.getOrDefault("spoiler", "false"));
                if (!url.isBlank()) {
                    items.add(new MediaGalleryItemSpec(url, description, spoiler));
                }
            }
            idx[0]++;
        }

        if (!items.isEmpty()) builder.addMediaGallery(new MediaGallerySpec(items));
    }

    private void parseAuthorBlock(PersistentEmbedSpec.Builder builder, List<String> lines, int[] idx, JdaTextReplacer replacer) {
        String name = "";
        String icon = "";
        String url = "";

        while (idx[0] < lines.size()) {
            String line = lines.get(idx[0]);
            Matcher close = CLOSE_TAG.matcher(line);
            if (close.matches() && "author".equalsIgnoreCase(close.group("tag"))) {
                idx[0]++;
                break;
            }

            Matcher tagLine = TAG_LINE.matcher(line);
            if (tagLine.matches()) {
                String content = apply(replacer, tagLine.group("content").trim());
                switch (tagLine.group("tag").toLowerCase(Locale.ROOT)) {
                    case "name" -> name = content;
                    case "icon" -> icon = content;
                    case "url" -> url = content;
                }
            }
            idx[0]++;
        }

        if (!name.isBlank()) builder.author(new EmbedAuthorSpec(name, icon, url));
    }

    private void parseFooterBlock(PersistentEmbedSpec.Builder builder, List<String> lines, int[] idx, JdaTextReplacer replacer) {
        String text = "";
        String icon = "";

        while (idx[0] < lines.size()) {
            String line = lines.get(idx[0]);
            Matcher close = CLOSE_TAG.matcher(line);
            if (close.matches() && "footer".equalsIgnoreCase(close.group("tag"))) {
                idx[0]++;
                break;
            }

            Matcher tagLine = TAG_LINE.matcher(line);
            if (tagLine.matches()) {
                String content = apply(replacer, tagLine.group("content").trim());
                switch (tagLine.group("tag").toLowerCase(Locale.ROOT)) {
                    case "text" -> text = content;
                    case "icon" -> icon = content;
                }
            }
            idx[0]++;
        }

        if (!text.isBlank()) builder.footer(new EmbedFooterSpec(text, icon));
    }

    private void skipBlock(String tag, List<String> lines, int[] idx) {
        while (idx[0] < lines.size()) {
            String line = lines.get(idx[0]);
            idx[0]++;
            Matcher close = CLOSE_TAG.matcher(line);
            if (close.matches() && tag.equalsIgnoreCase(close.group("tag"))) {
                break;
            }
        }
    }

    private PersistentButtonSpec buildButton(Map<String, String> attrs, String label) {
        PersistentButtonStyle style = PersistentButtonStyle.fromRaw(attrs.get("style"));
        String id = attrs.getOrDefault("id", "");
        String url = attrs.getOrDefault("url", "");
        boolean disabled = "true".equalsIgnoreCase(attrs.getOrDefault("disabled", "false"));
        String safeLabel = (label == null || label.isBlank()) ? "Button" : label;

        if (style == PersistentButtonStyle.LINK && url.isBlank()) {
            throw new IllegalArgumentException("Link button requires a non-empty 'url' attribute");
        }
        if (style != PersistentButtonStyle.LINK && id.isBlank()) {
            throw new IllegalArgumentException("Interactive button requires a non-empty 'id' attribute");
        }

        return new PersistentButtonSpec(id, safeLabel, style, disabled, url);
    }

    private Map<String, String> parseAttrs(String raw) {
        Map<String, String> attrs = new LinkedHashMap<>();
        if (raw == null || raw.isBlank()) return attrs;
        Matcher m = ATTR_PATTERN.matcher(raw);
        while (m.find()) {
            attrs.put(m.group(1).toLowerCase(Locale.ROOT), m.group(2));
        }
        return attrs;
    }

    private String apply(JdaTextReplacer replacer, String input) {
        if (input == null) return "";
        return replacer == null ? input : replacer.apply(input);
    }

    private int parseInt(String raw) {
        try {
            return Integer.parseInt(raw.trim());
        } catch (Exception ignored) {
            return 0;
        }
    }

    private List<String> preprocess(List<String> raw) {
        List<String> out = new ArrayList<>(raw.size());
        for (String s : raw) {
            if (s == null) continue;
            String line = s.strip();
            if (line.endsWith(",")) {
                line = line.substring(0, line.length() - 1).strip();
            }
            if (line.length() >= 2 && line.startsWith("\"") && line.endsWith("\"")) {
                line = line.substring(1, line.length() - 1).strip();
            }
            if (!line.isBlank()) {
                out.add(line);
            }
        }
        return out;
    }
}
