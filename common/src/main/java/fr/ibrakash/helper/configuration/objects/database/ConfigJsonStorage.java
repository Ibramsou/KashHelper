package fr.ibrakash.helper.configuration.objects.database;

import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ConfigJsonStorage {

    private String folder = "storage";

    public ConfigJsonStorage() {}

    public String getFolder() {
        return folder;
    }

    public ConfigJsonStorage folder(String folder) {
        this.folder = folder;
        return this;
    }
}
