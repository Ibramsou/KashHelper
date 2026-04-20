package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

final class ReflectiveBlobSerializer<T> implements PersistedBlobSerializer<T> {

    private final Class<T> type;
    private final Constructor<T> constructor;
    private final List<Field> fields;

    ReflectiveBlobSerializer(Class<T> type) {
        this.type = type;
        try {
            this.constructor = type.getDeclaredConstructor();
            this.constructor.setAccessible(true);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("ReflectiveBlobSerializer requires a no-arg constructor on " + type.getName(), e);
        }
        this.fields = collectFields(type);
    }

    @Override
    public byte[] serialize(T value) {
        if (value == null) return new byte[0];
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             DataOutputStream out = new DataOutputStream(baos)) {
            for (Field field : this.fields) {
                writeField(out, field, field.get(value));
            }
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to reflectively serialize " + type.getSimpleName(), e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return defaultValue();
        try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
            T instance = constructor.newInstance();
            for (Field field : this.fields) {
                Object value = readField(in, field);
                field.set(instance, value);
            }
            return instance;
        } catch (Exception e) {
            throw new RuntimeException("Failed to reflectively deserialize " + type.getSimpleName(), e);
        }
    }

    @Override
    public T defaultValue() {
        try {
            return constructor.newInstance();
        } catch (Exception e) {
            return null;
        }
    }

    private void writeField(DataOutputStream out, Field field, Object value) throws IOException {
        Class<?> t = field.getType();

        // Nullable marker for object types
        if (!t.isPrimitive()) {
            out.writeBoolean(value == null);
            if (value == null) return;
        }

        if (t == int.class || t == Integer.class) {
            out.writeInt((Integer) value);
        } else if (t == long.class || t == Long.class) {
            out.writeLong((Long) value);
        } else if (t == boolean.class || t == Boolean.class) {
            out.writeBoolean((Boolean) value);
        } else if (t == float.class || t == Float.class) {
            out.writeFloat((Float) value);
        } else if (t == double.class || t == Double.class) {
            out.writeDouble((Double) value);
        } else if (t == short.class || t == Short.class) {
            out.writeShort((Short) value);
        } else if (t == byte.class || t == Byte.class) {
            out.writeByte((Byte) value);
        } else if (t == char.class || t == Character.class) {
            out.writeChar((Character) value);
        } else if (t == String.class) {
            byte[] strBytes = ((String) value).getBytes(StandardCharsets.UTF_8);
            out.writeInt(strBytes.length);
            out.write(strBytes);
        } else if (t == UUID.class) {
            UUID uuid = (UUID) value;
            out.writeLong(uuid.getMostSignificantBits());
            out.writeLong(uuid.getLeastSignificantBits());
        } else if (t.isEnum()) {
            out.writeInt(((Enum<?>) value).ordinal());
        } else {
            throw new IOException("Unsupported field type for reflective blob serialization: " + t.getName());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Object readField(DataInputStream in, Field field) throws IOException {
        Class<?> t = field.getType();

        if (!t.isPrimitive()) {
            boolean isNull = in.readBoolean();
            if (isNull) return null;
        }

        if (t == int.class || t == Integer.class) return in.readInt();
        if (t == long.class || t == Long.class) return in.readLong();
        if (t == boolean.class || t == Boolean.class) return in.readBoolean();
        if (t == float.class || t == Float.class) return in.readFloat();
        if (t == double.class || t == Double.class) return in.readDouble();
        if (t == short.class || t == Short.class) return in.readShort();
        if (t == byte.class || t == Byte.class) return in.readByte();
        if (t == char.class || t == Character.class) return in.readChar();
        if (t == String.class) {
            int len = in.readInt();
            byte[] strBytes = new byte[len];
            in.readFully(strBytes);
            return new String(strBytes, StandardCharsets.UTF_8);
        }
        if (t == UUID.class) {
            long most = in.readLong();
            long least = in.readLong();
            return new UUID(most, least);
        }
        if (t.isEnum()) {
            int ordinal = in.readInt();
            Object[] constants = t.getEnumConstants();
            if (ordinal >= 0 && ordinal < constants.length) return constants[ordinal];
            return null;
        }
        throw new IOException("Unsupported field type for reflective blob deserialization: " + t.getName());
    }

    static boolean canHandle(Class<?> type) {
        try {
            type.getDeclaredConstructor();
        } catch (NoSuchMethodException e) {
            return false;
        }
        List<Field> fields = collectFields(type);
        if (fields.isEmpty()) return false;
        for (Field field : fields) {
            if (!isSupportedFieldType(field.getType())) return false;
        }
        return true;
    }

    private static boolean isSupportedFieldType(Class<?> type) {
        if (type.isPrimitive()) return true;
        if (type == String.class || type == UUID.class) return true;
        if (type == Integer.class || type == Long.class || type == Boolean.class) return true;
        if (type == Float.class || type == Double.class) return true;
        if (type == Short.class || type == Byte.class || type == Character.class) return true;
        return type.isEnum();
    }

    private static List<Field> collectFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (Class<?> cursor = type; cursor != null && cursor != Object.class; cursor = cursor.getSuperclass()) {
            for (Field field : cursor.getDeclaredFields()) {
                int mod = field.getModifiers();
                if (Modifier.isStatic(mod) || Modifier.isTransient(mod) || field.isSynthetic()) continue;
                if (!seen.add(field.getName())) continue;
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }
}

