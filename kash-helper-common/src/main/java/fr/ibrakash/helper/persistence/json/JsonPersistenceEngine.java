package fr.ibrakash.helper.persistence.json;
import fr.ibrakash.helper.persistence.PersistenceEngine;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.platform.KashAddon;
import java.io.File;
public class JsonPersistenceEngine implements PersistenceEngine {
    private final File storageFolder;
    public JsonPersistenceEngine(KashAddon<?> addon, String baseFolder) {
        String folder = baseFolder == null || baseFolder.isBlank() ? "storage" : baseFolder;
        this.storageFolder = new File(addon.getAddonFolder(), folder);
    }
    @Override
    public PersistenceType type() {
        return PersistenceType.JSON;
    }
    @Override
    public File getStorageFolder() {
        return this.storageFolder;
    }
}
