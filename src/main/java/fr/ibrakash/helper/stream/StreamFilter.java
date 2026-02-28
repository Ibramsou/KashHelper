 package fr.ibrakash.helper.stream;

import com.google.common.base.Supplier;
import fr.ibrakash.helper.configuration.objects.stream.ConfigFilter;
import fr.ibrakash.helper.configuration.objects.stream.ConfigFilterMode;
import fr.ibrakash.helper.configuration.objects.stream.ConfigStream;
import fr.ibrakash.helper.gui.GuiWrapper;

import javax.naming.Name;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

public class StreamFilter<V> {

    public static <V> StreamFilter<V> of(ConfigStream config) {
        return new StreamFilter<>(config);
    }

    public static <V> StreamFilter<V> of(GuiWrapper<?, ?, ?> wrapper) {
        return new StreamFilter<>(wrapper);
    }

    protected final Map<String, ConfigFilterMode> filterModes = new HashMap<>();
    private final Map<String, StreamConsumer<V>> combiners = new LinkedHashMap<>();
    protected final ConfigStream config;

    private Function<V, String> nameFunction;

    StreamFilter(GuiWrapper<?, ?, ?> wrapper) {
        this.config = wrapper.getConfig();
        this.setupGui(wrapper);
        this.defaultStreams();
    }

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

    public StreamFilter<V> setupGui(GuiWrapper<?, ?, ?> wrapper) {
        this.config.loadFilters(( filter) -> {
            final String id = filter.getFilterId();
            wrapper.setAction("change_filter_mode:" + id, (issuer, type, event, item) -> {
                StreamFilterUtil.changeFilterMode(this, filter.getFilterId(), !type.isRightClick());
                wrapper.refresh();
            });
            wrapper.replacer().add("%filter_lore:" + id + "%", () -> StreamFilterUtil.getModesInfo(this, id));
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
        StreamCombiner<V> combiner = null;
        for (ConfigFilter configFilter : this.config.getFilters().values()) {
            if (combiner == null) combiner = new StreamCombiner<>(stream);
            if (configFilter == null || configFilter.getModes().isEmpty()) return defaultValue.get();
            ConfigFilterMode mode = this.filterModes.getOrDefault(configFilter.getFilterId(), configFilter.getModes().getFirst());
            if (mode == null || mode.getStreams().isEmpty()) continue;
            for (String filterMode : mode.getStreams()) {
                this.combiners.get(filterMode).accept(combiner);
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

    @FunctionalInterface
    public interface StreamConsumer<V> {
        void accept(StreamCombiner<V> combiner);
    }
}
