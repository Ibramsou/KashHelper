package fr.ibrakash.helper.persistence.entity;

/**
 * Marker serializer used as the default value in {@link PersistedBlob}.
 */
public final class NoPersistedBlobSerializer implements PersistedBlobSerializer<Object> {

    @Override
    public byte[] serialize(Object value) {
        throw new UnsupportedOperationException("NoPersistedBlobSerializer is a marker and cannot serialize values");
    }

    @Override
    public Object deserialize(byte[] bytes) {
        throw new UnsupportedOperationException("NoPersistedBlobSerializer is a marker and cannot deserialize values");
    }
}

