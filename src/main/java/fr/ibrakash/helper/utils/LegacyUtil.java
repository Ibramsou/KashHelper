package fr.ibrakash.helper.utils;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;

public class LegacyUtil {

    public static Sound getSound(String input) {
        return deserializeSound(input).key();
    }

    public static Tuple<Sound, SoundCategory> deserializeSound(String input) {
        String soundName = input;
        SoundCategory category = null;

        // Try to get sound category if defined
        if (soundName.contains("/")) {
            String[] split = soundName.split("/");
            soundName = split[0];
            String categoryName = split[1];
            if (!categoryName.isEmpty()) {
                try {
                    category = SoundCategory.valueOf(categoryName.toUpperCase());
                } catch (Exception exception) {
                    throw new IllegalStateException(categoryName + " isn't a valid sound category name.");
                }
            }
        }

        // Parse sound key
        NamespacedKey key;
        if (soundName.contains(":")) {
            key = NamespacedKey.fromString(soundName);
        } else {
            key = NamespacedKey.minecraft(soundName);
        }

        // Try to get from modern format
        if (key != null) {
            Sound sound = Registry.SOUND_EVENT.get(key);
            if (sound != null) return Tuple.of(sound, category);
        }

        // Try to get from legacy format
        try {
            return Tuple.of((Sound) Sound.class.getField(soundName.toUpperCase()).get(null), category);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalStateException(soundName + " isn't a valid sound name.");
        }
    }
}
