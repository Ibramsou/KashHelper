package fr.ibrakash.helper.jda.platform;

import fr.ibrakash.helper.platform.KashAddon;
import net.dv8tion.jda.api.JDA;

import java.io.File;

/**
 * {@link KashAddon} implementation for the JDA platform.
 * The "raw" object is the {@link JDA} instance (or a bot wrapper).
 * The addon folder is resolved relative to a configurable working directory.
 */
public class KashJdaAddon extends KashAddon<JDA> {

    private final File addonFolder;

    public KashJdaAddon(JDA jda, File addonFolder) {
        super(jda);
        this.addonFolder = addonFolder;
    }

    @Override
    public File getAddonFolder() {
        return this.addonFolder;
    }
}

