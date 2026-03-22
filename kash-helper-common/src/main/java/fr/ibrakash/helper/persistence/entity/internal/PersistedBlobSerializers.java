package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.persistence.entity.NoPersistedBlobSerializer;
import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

final class PersistedBlobSerializers {

    private static final Map<Class<?>, PersistedBlobSerializer<?>> DEFAULTS = new HashMap<>();

    static {
        DEFAULTS.put(byte[].class, new BytesSerializer());
        DEFAULTS.put(String.class, new StringSerializer());
        DEFAULTS.put(Integer.class, new IntegerSerializer());
        DEFAULTS.put(int.class, new IntegerSerializer());
        DEFAULTS.put(Long.class, new LongSerializer());
        DEFAULTS.put(long.class, new LongSerializer());
        DEFAULTS.put(Boolean.class, new BooleanSerializer());
        DEFAULTS.put(boolean.class, new BooleanSerializer());
        DEFAULTS.put(Double.class, new DoubleSerializer());
        DEFAULTS.put(double.class, new DoubleSerializer());
        DEFAULTS.put(Float.class, new FloatSerializer());
        DEFAULTS.put(float.class, new FloatSerializer());
        DEFAULTS.put(UUID.class, new UuidSerializer());
    }

    private PersistedBlobSerializers() {
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static PersistedBlobSerializer<Object> resolve(Class<?> fieldType,
                                                   Class<? extends PersistedBlobSerializer<?>> serializerClass) {
        if (serializerClass != null && serializerClass != NoPersistedBlobSerializer.class) {
            try {
                return (PersistedBlobSerializer<Object>) serializerClass.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new IllegalArgumentException("Unable to instantiate blob serializer " + serializerClass.getName(), e);
            }
        }

        PersistedBlobSerializer<?> serializer = DEFAULTS.get(fieldType);
        if (serializer != null) {
            return (PersistedBlobSerializer<Object>) serializer;
        }

        if (fieldType.isEnum()) {
            return new EnumSerializer((Class<? extends Enum>) fieldType);
        }

        return null;
    }

    private static final class BytesSerializer implements PersistedBlobSerializer<Object> {
        @Override
        public byte[] serialize(Object value) {
            return value == null ? new byte[0] : (byte[]) value;
        }

        @Override
        public Object deserialize(byte[] bytes) {
            return bytes == null ? new byte[0] : bytes;
        }
    }

    private static final class StringSerializer implements PersistedBlobSerializer<Object> {
        @Override
        public byte[] serialize(Object value) {
            return value == null ? new byte[0] : ((String) value).getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public Object deserialize(byte[] bytes) {
            return bytes == null ? "" : new String(bytes, StandardCharsets.UTF_8);
        }
    }

    private static final class IntegerSerializer implements PersistedBlobSerializer<Object> {
        @Override
        public byte[] serialize(Object value) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 DataOutputStream data = new DataOutputStream(out)) {
                data.writeInt(value == null ? 0 : (Integer) value);
                return out.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) return 0;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                return in.readInt();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class LongSerializer implements PersistedBlobSerializer<Object> {
        @Override
        public byte[] serialize(Object value) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 DataOutputStream data = new DataOutputStream(out)) {
                data.writeLong(value == null ? 0L : (Long) value);
                return out.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) return 0L;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                return in.readLong();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class BooleanSerializer implements PersistedBlobSerializer<Object> {
        @Override
        public byte[] serialize(Object value) {
            return new byte[] {(byte) (((value != null) && ((Boolean) value)) ? 1 : 0)};
        }

        @Override
        public Object deserialize(byte[] bytes) {
            return bytes != null && bytes.length > 0 && bytes[0] != 0;
        }
    }

    private static final class DoubleSerializer implements PersistedBlobSerializer<Object> {
        @Override
        public byte[] serialize(Object value) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 DataOutputStream data = new DataOutputStream(out)) {
                data.writeDouble(value == null ? 0D : (Double) value);
                return out.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) return 0D;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                return in.readDouble();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class FloatSerializer implements PersistedBlobSerializer<Object> {
        @Override
        public byte[] serialize(Object value) {
            try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                 DataOutputStream data = new DataOutputStream(out)) {
                data.writeFloat(value == null ? 0F : (Float) value);
                return out.toByteArray();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Object deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) return 0F;
            try (DataInputStream in = new DataInputStream(new ByteArrayInputStream(bytes))) {
                return in.readFloat();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static final class UuidSerializer implements PersistedBlobSerializer<Object> {
        @Override
        public byte[] serialize(Object value) {
            return value == null ? new byte[0] : value.toString().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public Object deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) return null;
            return UUID.fromString(new String(bytes, StandardCharsets.UTF_8));
        }
    }

    private static final class EnumSerializer implements PersistedBlobSerializer<Object> {

        private final Class<? extends Enum> enumType;

        private EnumSerializer(Class<? extends Enum> enumType) {
            this.enumType = enumType;
        }

        @Override
        public byte[] serialize(Object value) {
            return value == null ? new byte[0] : ((Enum<?>) value).name().getBytes(StandardCharsets.UTF_8);
        }

        @Override
        public Object deserialize(byte[] bytes) {
            if (bytes == null || bytes.length == 0) return null;
            return Enum.valueOf(this.enumType, new String(bytes, StandardCharsets.UTF_8));
        }
    }
}

