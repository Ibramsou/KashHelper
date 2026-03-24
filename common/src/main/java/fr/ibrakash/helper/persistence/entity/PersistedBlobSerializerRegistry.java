package fr.ibrakash.helper.persistence.entity;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class PersistedBlobSerializerRegistry {

    private static final Map<Type, Supplier<? extends PersistedBlobSerializer<?>>> REGISTRY = new ConcurrentHashMap<>();

    private PersistedBlobSerializerRegistry() {
    }

    public static void register(Class<?> type, Supplier<? extends PersistedBlobSerializer<?>> factory) {
        register((Type) type, factory);
    }

    public static void register(Type type, Supplier<? extends PersistedBlobSerializer<?>> factory) {
        REGISTRY.put(Objects.requireNonNull(type, "type"), Objects.requireNonNull(factory, "factory"));
    }

    public static void unregister(Class<?> type) {
        unregister((Type) type);
    }

    public static void unregister(Type type) {
        REGISTRY.remove(type);
    }

    public static Type parameterizedType(Class<?> rawType, Type... actualTypeArguments) {
        return new SimpleParameterizedType(rawType, actualTypeArguments);
    }

    @SuppressWarnings("unchecked")
    public static PersistedBlobSerializer<Object> resolve(Type type) {
        Supplier<? extends PersistedBlobSerializer<?>> factory = REGISTRY.get(type);
        if (factory != null) {
            return (PersistedBlobSerializer<Object>) factory.get();
        }

        for (Map.Entry<Type, Supplier<? extends PersistedBlobSerializer<?>>> entry : REGISTRY.entrySet()) {
            if (matches(entry.getKey(), type)) {
                return (PersistedBlobSerializer<Object>) entry.getValue().get();
            }
        }

        return null;
    }

    private static boolean matches(Type registeredType, Type requestedType) {
        if (Objects.equals(registeredType, requestedType)) {
            return true;
        }
        if (!(registeredType instanceof ParameterizedType registered) || !(requestedType instanceof ParameterizedType requested)) {
            return false;
        }
        if (!Arrays.equals(registered.getActualTypeArguments(), requested.getActualTypeArguments())) {
            return false;
        }
        if (!(registered.getRawType() instanceof Class<?> registeredRaw) || !(requested.getRawType() instanceof Class<?> requestedRaw)) {
            return false;
        }
        return registeredRaw.isAssignableFrom(requestedRaw);
    }

    private static final class SimpleParameterizedType implements ParameterizedType {

        private final Class<?> rawType;
        private final Type[] actualTypeArguments;

        private SimpleParameterizedType(Class<?> rawType, Type[] actualTypeArguments) {
            this.rawType = Objects.requireNonNull(rawType, "rawType");
            this.actualTypeArguments = Objects.requireNonNull(actualTypeArguments, "actualTypeArguments").clone();
        }

        @Override
        public Type[] getActualTypeArguments() {
            return actualTypeArguments.clone();
        }

        @Override
        public Type getRawType() {
            return rawType;
        }

        @Override
        public Type getOwnerType() {
            return null;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof ParameterizedType other)) return false;
            return Objects.equals(rawType, other.getRawType())
                    && Objects.equals(getOwnerType(), other.getOwnerType())
                    && Arrays.equals(actualTypeArguments, other.getActualTypeArguments());
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(actualTypeArguments) ^ Objects.hashCode(rawType) ^ Objects.hashCode(getOwnerType());
        }

        @Override
        public String toString() {
            String args = Arrays.stream(actualTypeArguments)
                    .map(Type::getTypeName)
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("");
            return rawType.getTypeName() + "<" + args + ">";
        }
    }
}

