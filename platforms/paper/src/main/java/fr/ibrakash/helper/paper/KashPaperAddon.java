package fr.ibrakash.helper.paper;

import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class KashPaperAddon extends KashAddon<JavaPlugin> {

    private boolean guiListenerRegistered;

    public KashPaperAddon(JavaPlugin raw) {
        super(raw);
    }

    @Override
    public File getAddonFolder() {
        return this.raw.getDataFolder();
    }

    public synchronized boolean markGuiListenerRegistered() {
        if (this.guiListenerRegistered) {
            return false;
        }
        this.guiListenerRegistered = true;
        return true;
    }
}
