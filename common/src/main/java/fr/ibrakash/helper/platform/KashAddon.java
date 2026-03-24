package fr.ibrakash.helper.platform;

import fr.ibrakash.helper.configuration.Configurations;

import java.io.File;

public abstract class KashAddon<V> {

    protected V raw;
    protected final KashPaths paths;
    protected final Configurations configurations;

    public KashAddon(V raw) {
        this.raw = raw;
        this.paths = new KashPaths(this);
        this.configurations = new Configurations(this);
    }

    public KashPaths paths() {
        return paths;
    }

    public Configurations configurations() {
        return this.configurations;
    }

    public V getRaw() {
        return raw;
    }

    public abstract File getAddonFolder();
}
