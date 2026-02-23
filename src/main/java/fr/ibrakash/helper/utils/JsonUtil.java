package fr.ibrakash.helper.utils;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class JsonUtil {
    private static final DefaultPrettyPrinter PRETTY_PRINTER = new DefaultPrettyPrinter();
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .setDefaultPrettyPrinter(PRETTY_PRINTER);

    static {
        PRETTY_PRINTER.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }

    public static void writeFile(JavaPlugin plugin, String path, Object object) {
        final File file = getFile(plugin, path);
        writeFile(file, object);
    }

    public static void writeFile(File file, Object object) {
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            MAPPER.writeValue(file, object);
        } catch (IOException e) {
            throw new RuntimeException("Could not write object to json file", e);
        }
    }

    public static <K, V> Map<K, V> readFileMap(JavaPlugin plugin, String path, Supplier<Map<K, V>> defaultValue , Class<K> keyClass, Class<V> valueClass) {
        return readFileMap(getFile(plugin, path), defaultValue, keyClass, valueClass);
    }

    public static <K, V> Map<K, V> readFileMap(File file, Supplier<Map<K, V>> defaultValue, Class<K> keyClass, Class<V> valueClass) {
        if (file == null || !file.exists()) return defaultValue.get();
        try {
            TypeFactory factory = TypeFactory.defaultInstance();
            MapType type = factory.constructMapType(HashMap.class, keyClass, valueClass);
            return MAPPER.readValue(file, type);
        } catch (IOException e) {
            throw new RuntimeException("Unable to read file map", e);
        }
    }

    public static <V> V readFile(JavaPlugin plugin, String path, Class<V> objectClass) {
        return readFile(plugin, path, objectClass, null);
    }

    public static <V> V readFile(JavaPlugin plugin, String path, Class<V> objectClass, Supplier<V> defaultValue) {
        File file = getFile(plugin, path);
        if (!file.exists()) return defaultValue.get();
        return readFile(file, objectClass, defaultValue);
    }

    public static <V> V readFile(File file, Class<V> objectClass, Supplier<V> defaultValue) {
        if (!file.exists()) return defaultValue.get();
        return readFile(file, objectClass);
    }

    public static <V> V readFile(File file, Class<V> objectClass) {
        try {
            return MAPPER.readValue(file, objectClass);
        } catch (IOException e) {
            throw new RuntimeException("Could not read object from json file", e);
        }
    }

    private static File getFile(JavaPlugin plugin, String path) {
        return new File(plugin.getDataFolder(), path + ".json");
    }
}
