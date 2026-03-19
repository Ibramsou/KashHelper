package fr.ibrakash.helper.text;

import fr.ibrakash.helper.platform.KashPlatform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public abstract class TextUtil<S> {

    public static TextUtil<?> get() {
        return KashPlatform.get().textUtil();
    }

    @SafeVarargs
    public static List<Object> mergeReplacers(List<Object> input, List<Object>... replacers) {
        List<Object> result = new ArrayList<>(input);
        for (List<Object> replacer : replacers) {
            result.addAll(replacer);
        }

        return result;
    }

    public static List<Object> mergeReplacers(List<Object> input, Object... replacers) {
        List<Object> result = new ArrayList<>(input);
        result.addAll(List.of(replacers));
        return result;
    }

    public static List<String> replacedList(List<String> input, Object... replacers) {
        return replacedList(input, Arrays.asList(replacers));
    }

    public static List<String> replacedList(List<String> input, List<Object> replacers) {
        List<String> results = new ArrayList<>();
        input.forEach(s -> results.add(replaced(s, replacers)));
        return results;
    }

    public static String replaced(String input, Object... replacers) {
        return replaced(input, Arrays.asList(replacers));
    }

    public static String replaced(String input, List<Object> replacers) {
        String result = input;
        for (int i = 0; i < replacers.size(); i += 2) {
            final String pattern = String.valueOf(replacers.get(i));
            if (i + 1 >= replacers.size()) {
                throw new IllegalArgumentException("No replacement found for " + pattern);
            }
            result = result.replace(pattern, String.valueOf(replacers.get(i + 1)));
        }
        return result;
    }

    public S replacedComponent(String input, Object... replacers) {
        return this.replacedComponent(input, Arrays.asList(replacers));
    }

    public abstract S replacedComponent(String input, List<Object> replacers);

    public List<S> replacedComponents(List<String> input, Object... replacers) {
        return this.replacedComponents(input, Arrays.asList(replacers));
    }

    public List<S> replacedComponents(List<String> input, List<Object> replacers) {
        return this.replacedComponents(input, replacers, null);
    }

    public abstract List<S> replacedComponents(List<String> input, List<Object> replacers, UnaryOperator<String> operator);

    public S replacedItemName(String input, Object... replacers) {
        return this.replacedItemName(input, Arrays.asList(replacers));
    }

    public abstract S replacedItemName(String input, List<Object> replacers);

    public List<S> replacedItemLore(List<String> input, Object... replacers) {
        return this.replacedItemLore(input, Arrays.asList(replacers));
    }

    public abstract List<S> replacedItemLore(List<String> input, List<Object> replacers);
}
