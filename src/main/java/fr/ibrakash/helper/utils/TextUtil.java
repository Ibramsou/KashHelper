package fr.ibrakash.helper.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;

public class TextUtil {

    public static List<String> replacedList(List<String> input, Object... replacers) {
        List<String> results = new ArrayList<>();
        input.forEach(s -> results.add(replaced(s, replacers)));
        return results;
    }

    public static List<Component> replacedComponents(List<String> input, Object... replacers) {
        List<Component> results = new ArrayList<>();
        input.forEach(s -> results.add(replacedComponent(s, replacers)));
        return results;
    }

    public static String replaced(String input, Object... replacers) {
        String result = input;
        for (int i = 0; i < replacers.length; i += 2) {
            final String pattern = String.valueOf(replacers[i]);
            if (i + 1 >= replacers.length) {
                throw new IllegalArgumentException("No replacement found for " + pattern);
            }
            result = result.replace(pattern, String.valueOf(replacers[i + 1]));
        }
        return result;
    }

    public static Component replacedComponent(String input, Object... replacers) {
        return MiniMessage.miniMessage().deserialize(replaced(input, replacers));
    }

    public static Component replacedItemComponent(String input, Object... replacers) {
        return replacedComponent("<italic:false>" + input, replacers);
    }

    public static List<Component> replacedItemComponents(List<String> input, Object... replacers) {
        List<Component> results = new ArrayList<>();
        input.forEach(s -> results.add(replacedItemComponent(s, replacers)));
        return results;
    }
}
