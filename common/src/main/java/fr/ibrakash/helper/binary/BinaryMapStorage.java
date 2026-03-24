package fr.ibrakash.helper.binary;

import java.util.Map;
import java.util.function.Consumer;

public abstract class BinaryMapStorage<K, V> extends BinaryStorage<Map<K, V>> {

    protected BinaryMapStorage(Map<K, V> value) {
        super(value);
    }

    public void put(K key, V value) {
        this.getUpdateValue(map -> map.put(key, value));
    }

    public void remove(K key) {
        this.getUpdateValue(map -> map.remove(key));
    }

    public void clearAll() {
        this.getUpdateValue(Map::clear);
    }

    public void updateAll(Consumer<Map<K, V>> updater) {
        this.getUpdateValue(updater);
    }
}

