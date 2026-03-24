package fr.ibrakash.helper.persistence.entity;

public interface PersistedBlobSerializer<T> {

    byte[] serialize(T value);

    T deserialize(byte[] bytes);

    default T defaultValue() {
        return null;
    }
}

