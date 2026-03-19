package fr.ibrakash.helper.text;

import fr.ibrakash.helper.platform.KashPlatform;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class TextReplacer {
    protected final Map<String, Object> replacers;
    protected final Map<String, Supplier<Object>> dynamicReplacers;

    protected TextReplacer() {
        this.replacers = new HashMap<>();
        this.dynamicReplacers = new HashMap<>();
    }

    public static TextReplacer create() {
        return KashPlatform.get().createTextReplacer();
    }

    public static TextReplacer of(TextReplacer replacer) {
        return create().merge(replacer);
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
        if (from == null) return this;
        if (!from.replacers.isEmpty()) {
            this.replacers.putAll(from.replacers);
        }
        if (!from.dynamicReplacers.isEmpty()) {
            this.dynamicReplacers.putAll(from.dynamicReplacers);
        }
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

    public abstract Object deserialize(String input);

    public abstract List<?> deserializeComponents(List<String> input);

    public abstract Object deserializeItemName(String input);

    public abstract List<?> deserializeItemLore(List<String> input);

    public <V> List<V> deserialize(List<String> input, Function<String, V> function) {
        List<V> result = new ArrayList<>();
        input.forEach(s -> result.add(function.apply(this.apply(s))));
        return result;
    }
}
