package fr.ibrakash.helper.text;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class TextUtil {

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

    public static List<Component> replacedComponents(List<String> input, Object... replacers) {
        return replacedComponents(input, Arrays.asList(replacers));
    }

    public static List<Component> replacedComponents(List<String> input, List<Object> replacers) {
        return replacedComponents(input, replacers, null);
    }

    public static List<Component> replacedComponents(List<String> input, List<Object> replacers, UnaryOperator<String> operator) {
        List<Component> results = new ArrayList<>();
        input.forEach(s -> results.add(replacedComponent(operator == null ? s : operator.apply(s), replacers)));
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
            result = result.replace(pattern, String.valueOf(replacers.get((i + 1))));
        }
        return result;
    }

    public static Component replacedComponent(String input, Object... replacers) {
        return replacedComponent(input, Arrays.asList(replacers));
    }

    public static Component replacedComponent(String input, List<Object> replacers) {
        return MiniMessage.miniMessage().deserialize(replaced(input, replacers));
    }

    public static Component replacedItemName(String input, Object... replacers) {
        return replacedItemName(input, Arrays.asList(replacers));
    }

    public static Component replacedItemName(String input, List<Object> replacers) {
        return replacedComponent("<italic:false>" + input, replacers);
    }

    public static List<Component> replacedItemLore(List<String> input, Object... replacers) {
        return replacedItemLore(input, Arrays.asList(replacers));
    }

    public static List<Component> replacedItemLore(List<String> input, List<Object> replacers) {
        List<Component> results = new ArrayList<>();
        input.forEach(s -> {
            String[] lines = replaced(s, replacers).split("\n");
            for (String line : lines) {
                results.add(MiniMessage.miniMessage().deserialize(line));
            }
        });
        return results;
    }
}
