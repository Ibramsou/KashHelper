package fr.ibrakash.helper.configuration.objects.database;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
@ConfigSerializable
public class ConfigMongo {
    private String uri = "mongodb://localhost:27017";
    private String database = "kashhelper";
    private String collectionPrefix = "kash_";
    public ConfigMongo() {
    }
    public String getUri() {
        return uri;
    }
    public String getDatabase() {
        return database;
    }
    public String getCollectionPrefix() {
        return collectionPrefix;
    }
    public ConfigMongo uri(String uri) {
        this.uri = uri;
        return this;
    }
    public ConfigMongo database(String database) {
        this.database = database;
        return this;
    }
    public ConfigMongo collectionPrefix(String collectionPrefix) {
        this.collectionPrefix = collectionPrefix;
        return this;
    }
}
