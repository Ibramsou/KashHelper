package fr.ibrakash.helper.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class TextReplacer {
    private final Map<String, Object> replacers;
    private final Map<String, Supplier<Object>> dynamicReplacers = new HashMap<>();

    public static TextReplacer create() {
        return new TextReplacer(new HashMap<>());
    }

    public static TextReplacer of(TextReplacer replacer) {
        return new TextReplacer(new HashMap<>(replacer.replacers));
    }

    TextReplacer(Map<String, Object> replacers) {
        this.replacers = replacers;
    }

    public TextReplacer add(String placeholder, Object value) {
        this.replacers.put(placeholder, value);
        return this;
    }

    public TextReplacer add(String placeholder, Supplier<Object> value) {
        this.dynamicReplacers.put(placeholder, value);
        return this;
    }

    public TextReplacer merge(TextReplacer from) {
        if (from.replacers.isEmpty()) return this;
        this.replacers.putAll(from.replacers);
        this.dynamicReplacers.putAll(from.dynamicReplacers);
        return this;
    }

    private String parseDynamicPlaceholders(String input) {
        String result = input;
        for (Map.Entry<String, Supplier<Object>> entry : this.dynamicReplacers.entrySet()) {
            result = result.replace(entry.getKey(), String.valueOf(entry.getValue().get()));
        }

        return result;
    }

    public String apply(String input) {
        String result = this.parseDynamicPlaceholders(input);
        for (Map.Entry<String, Object> entry : this.replacers.entrySet()) {
            result = result.replace(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return result;
    }

    public List<String> applyList(List<String> input) {
        return deserialize(input, s -> s);
    }

    public Component deserialize(String input) {
        return MiniMessage.miniMessage().deserialize(this.apply(input));
    }

    public List<Component> deserializeComponents(List<String> input) {
        return this.deserialize(input, s -> MiniMessage.miniMessage().deserialize(s));
    }

    public Component deserializeItemName(String input) {
        return deserialize("<italic:false>" + input);
    }

    public List<Component> deserializeItemLore(List<String> input) {
        List<Component> result = new ArrayList<>();
        input.forEach(s -> {
            String[] lines = this.apply(s).split("\n");
            for (String line : lines) {
                result.add(MiniMessage.miniMessage().deserialize("<italic:false>" + line));
            }
        });

        return result;
    }

    public <V> List<V> deserialize(List<String> input, Function<String, V> function) {
        List<V> result = new ArrayList<>();
        input.forEach(s -> result.add(function.apply(this.apply(s))));
        return result;
    }
}
