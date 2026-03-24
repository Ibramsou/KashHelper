package fr.ibrakash.helper.utils;

public record Tuple<K, V>(K key, V value) {

    public static <K, V> Tuple<K, V> of(K key, V value) {
        return new Tuple<K, V>(key, value);
    }
}
