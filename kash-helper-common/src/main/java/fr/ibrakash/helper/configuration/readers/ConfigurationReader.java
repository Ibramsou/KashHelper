package fr.ibrakash.helper.configuration.readers;

import fr.ibrakash.helper.configuration.ConfigurationLoaderType;
import fr.ibrakash.helper.platform.KashAddon;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public abstract class ConfigurationReader {

    protected final KashAddon<?> addon;
    protected final String key;

    protected ConfigurationNode node;

    protected ConfigurationReader(KashAddon<?> addon) {
        this.addon = addon;
        this.key = this.key();
    }

    public void reload() {
        this.node = this.addon.configurations().loadReadOnlyNode(ConfigurationLoaderType.YAML, this.addon.paths().get(this.key, "yml"));
        if (!this.node.isMap()) return;
        if (!this.preLoad()) return;

        List<NodeFilter> filters = this.nodeFilters();
        boolean hasFilters = filters != null && !filters.isEmpty();
        this.walkNodesRecursively("", this.node, filters == null ? Collections.emptyList() : filters, hasFilters);
    }

    private void walkNodesRecursively(String parentPath, ConfigurationNode current, List<NodeFilter> filters, boolean hasFilters) {
        current.childrenMap().forEach((mapKey, child) -> {
            String nodeName = String.valueOf(mapKey);
            String path = parentPath.isEmpty() ? nodeName : parentPath + "." + nodeName;

            if (this.shouldReadNode(path, nodeName, child, filters, hasFilters)) {
                this.readSafely(path, nodeName, child, hasFilters);
            }
            this.walkNodesRecursively(path, child, filters, hasFilters);
        });

        for (int i = 0; i < current.childrenList().size(); i++) {
            ConfigurationNode child = current.childrenList().get(i);
            String nodeName = String.valueOf(i);
            String path = parentPath.isEmpty() ? nodeName : parentPath + "." + nodeName;

            if (this.shouldReadNode(path, nodeName, child, filters, hasFilters)) {
                this.readSafely(path, nodeName, child, hasFilters);
            }
            this.walkNodesRecursively(path, child, filters, hasFilters);
        }
    }

    private boolean shouldReadNode(String path, String nodeName, ConfigurationNode node, List<NodeFilter> filters, boolean hasFilters) {
        if (!hasFilters) return true;
        return filters.stream().anyMatch(filter -> filter.matches(path, nodeName, node));
    }

    private void readSafely(String path, String nodeName, ConfigurationNode node, boolean strictMode) {
        try {
            this.readNode(nodeName, node);
        } catch (SerializationException e) {
            if (strictMode) {
                throw new RuntimeException("Invalid configuration for key '" + path + "': " + e.getMessage(), e);
            }
        }
    }

    public final <V> V resolveValue(Map<String, V> map, String path, NodeFunction<V> nodeFunction, Supplier<V> fallback) {
        if (this.preLoad()) {
            V result = map.get(path);
            if (result == null) return fallback.get();
            return result;
        }


        return map.computeIfAbsent(path, s -> {
            ConfigurationNode node = resolvePath(path);
            if (node.empty()) return fallback.get();
            try {
                V result = nodeFunction.apply(node);
                if (result == null) return fallback.get();
                return result;
            } catch (SerializationException e) {
                throw new IllegalArgumentException("Invalid configuration: " + path, e);
            }
        });
    }

    public final ConfigurationNode resolvePath(String path) {
        String[] split = path.split("\\.");
        return this.node.node((Object[]) split);
    }

    public void readNode(String nodeName, ConfigurationNode node) throws SerializationException {
        throw new UnsupportedOperationException("Method implementation not defined");
    }

    public boolean preLoad() {
        return false;
    }

    public abstract String key();

    protected List<NodeFilter> nodeFilters() {
        return Collections.emptyList();
    }

    @FunctionalInterface
    public interface NodeFilter {
        boolean matches(String path, String nodeName, ConfigurationNode node);
    }

    @FunctionalInterface
    public interface NodeFunction<V> {
        V apply(ConfigurationNode node) throws SerializationException;
    }
}
