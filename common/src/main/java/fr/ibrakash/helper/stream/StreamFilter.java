package fr.ibrakash.helper.stream;

import fr.ibrakash.helper.configuration.objects.stream.ConfigFilter;
import fr.ibrakash.helper.configuration.objects.stream.ConfigFilterMode;
import fr.ibrakash.helper.configuration.objects.stream.ConfigStream;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class StreamFilter<V> {

    public static <V> StreamFilter<V> of(ConfigStream config) {
        return new StreamFilter<>(config);
    }

    public static <V> StreamFilter<V> of(Object wrapper) {
        StreamFilter<V> filter = new StreamFilter<>(extractConfig(wrapper));
        filter.setupGui(wrapper);
        return filter;
    }

    protected final Map<String, ConfigFilterMode> filterModes = new HashMap<>();
    private final Map<String, StreamConsumer<V>> combiners = new LinkedHashMap<>();
    protected final ConfigStream config;

    private Function<V, String> nameFunction;

    StreamFilter(ConfigStream config) {
        this.config = config;
        this.config.loadFilters(configFilter -> {});
        this.defaultStreams();
    }

    private void defaultStreams() {
        this.add("alphabetical_asc_sort", combiner ->
                combiner.sort(Comparator.comparing(v -> this.getDefaultNameFunction(v).apply(v))));
        this.add("alphabetical_desc_sort", combiner ->
                combiner.sort(Comparator.<V, String>comparing(v -> this.getDefaultNameFunction(v).apply(v)).reversed()));
    }

    public StreamFilter<V> setupGui(Object wrapper) {
        this.config.loadFilters((filter) -> {
            final String id = filter.getFilterId();
            this.registerAction(wrapper, id);
            this.registerLoreReplacer(wrapper, id);
        });
        return this;
    }

    public StreamFilter<V> defaultName(Function<V, String> defaultName) {
        this.nameFunction = defaultName;
        return this;
    }

    public StreamFilter<V> add(String streamName, StreamConsumer<V> combiner) {
        this.combiners.put(streamName, combiner);
        return this;
    }

    public List<V> applyFilters(List<V> objets) {
        return this.applyFilters(objets.stream(), () -> objets);
    }

    public List<V> applyFilters(Stream<V> stream) {
        return applyFilters(stream, stream::toList);
    }

    private List<V> applyFilters(Stream<V> stream, Supplier<List<V>> defaultValue) {
        if (this.config.getFilters().isEmpty()) return defaultValue.get();
        StreamConsumer<V> defaultConsumer = this.combiners.get("default");
        StreamCombiner<V> combiner = defaultConsumer == null ? null : new StreamCombiner<>(stream);
        if (defaultConsumer != null) defaultConsumer.accept(combiner);
        for (ConfigFilter configFilter : this.config.getFilters().values()) {
            if (combiner == null) combiner = new StreamCombiner<>(stream);
            if (configFilter == null || configFilter.getModes().isEmpty()) return defaultValue.get();
            ConfigFilterMode mode = this.filterModes.getOrDefault(configFilter.getFilterId(), configFilter.getModes().getFirst());
            if (mode == null || mode.getStreams().isEmpty()) continue;
            for (String filterMode : mode.getStreams()) {
                StreamConsumer<V> consumer = this.combiners.get(filterMode);
                if (consumer == null) {
                    System.out.println("Filter type " + filterMode + " not found");
                    continue;
                }
                consumer.accept(combiner);
            }
        }

        return combiner != null ? combiner.build().toList() : stream.toList();
    }

    private Function<V, String> getDefaultNameFunction(V object) {
        if (this.nameFunction != null) return this.nameFunction;
        if (object instanceof Enum<?>) {
            this.nameFunction = v -> ((Enum<?>) v).name();
        } else {
            this.nameFunction = Object::toString;
        }

        return this.nameFunction;
    }

    private void registerAction(Object wrapper, String id) {
        try {
            Method actionMethod = findMethod(wrapper.getClass(), "action", 2);
            Class<?> consumerType = actionMethod.getParameterTypes()[1];
            Object consumer = Proxy.newProxyInstance(
                    consumerType.getClassLoader(),
                    new Class[]{consumerType},
                    (proxy, method, args) -> {
                        if (!method.getName().equals("doAction")) return null;
                        Object clickType = args != null && args.length > 1 ? args[1] : null;
                        boolean forward = clickType == null || !Boolean.TRUE.equals(invokeNoArgs(clickType, "isRightClick"));
                        StreamFilterUtil.changeFilterMode(this, id, forward);
                        invokeNoArgs(wrapper, "refresh");
                        return null;
                    }
            );
            actionMethod.invoke(wrapper, "change_filter_mode:" + id, consumer);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to bind stream filter actions to gui wrapper", e);
        }
    }

    private void registerLoreReplacer(Object wrapper, String id) {
        try {
            Object replacer = invokeNoArgs(wrapper, "replacer");
            Method addMethod = findMethod(replacer.getClass(), "add", 2);
            addMethod.invoke(replacer, "%filter_lore:" + id + "%", (Supplier<Object>) () -> StreamFilterUtil.getModesInfo(this, id));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to bind stream filter lore replacer to gui wrapper", e);
        }
    }

    private static ConfigStream extractConfig(Object wrapper) {
        Object config = invokeNoArgs(wrapper, "getConfig");
        if (config instanceof ConfigStream configStream) {
            return configStream;
        }
        throw new IllegalArgumentException("Wrapper config must extend ConfigStream");
    }

    private static Object invokeNoArgs(Object target, String methodName) {
        try {
            Method method = findMethod(target.getClass(), methodName, 0);
            return method.invoke(target);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to invoke method '" + methodName + "' on " + target.getClass().getName(), e);
        }
    }

    private static Method findMethod(Class<?> type, String methodName, int parameterCount) throws NoSuchMethodException {
        Class<?> current = type;
        while (current != null) {
            for (Method method : current.getDeclaredMethods()) {
                if (method.getName().equals(methodName) && method.getParameterCount() == parameterCount) {
                    method.setAccessible(true);
                    return method;
                }
            }
            current = current.getSuperclass();
        }
        throw new NoSuchMethodException(methodName);
    }

    @FunctionalInterface
    public interface StreamConsumer<V> {
        void accept(StreamCombiner<V> combiner);
    }
}
