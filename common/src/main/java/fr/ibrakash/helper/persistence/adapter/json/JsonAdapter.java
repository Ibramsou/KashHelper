package fr.ibrakash.helper.persistence.adapter.json;

import fr.ibrakash.helper.persistence.adapter.DatabaseAdapter;
import fr.ibrakash.helper.persistence.adapter.DatabaseRepository;
import fr.ibrakash.helper.utils.JsonUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public abstract class JsonAdapter<R extends DatabaseRepository> extends DatabaseAdapter<R> {

    protected JsonAdapter(R repository) {
        super(repository);
    }

    /**
     * Loads an entire JSON file into {@code target}.
     *
     * <p>The file is resolved as {@code <storageFolder>/<snake_entity_name>.json}
     * (e.g. {@code PlayerProfile} → {@code player_profile.json}).
     *
     * @param target      the map to populate (must be mutable)
     * @param keyClass    the JSON key type
     * @param entityClass the JSON value type
     * @param <K>         key type
     * @param <V>         value / entity type
     */
    public <K, V> void loadEntireData(Map<K, V> target, Class<K> keyClass, Class<V> entityClass) {
        File folder = repository.storageFolder();
        File file = new File(folder, snake(entityClass.getSimpleName()) + ".json");
        Map<K, V> loaded = JsonUtil.readFileMap(file, HashMap::new, keyClass, entityClass);
        target.putAll(loaded);
    }

    /**
     * Writes the whole {@code source} map to a JSON file named after {@code entityClass}.
     */
    public <K, V> void flushEntireData(Map<K, V> source, Class<V> entityClass) {
        File folder = repository.storageFolder();
        File file = new File(folder, snake(entityClass.getSimpleName()) + ".json");
        JsonUtil.writeFile(file, source);
    }

    // -------------------------------------------------------------------------
    // Utility
    // -------------------------------------------------------------------------

    private static String snake(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (Character.isUpperCase(c) && i > 0) sb.append('_');
            sb.append(Character.toLowerCase(c));
        }
        return sb.toString();
    }
}
