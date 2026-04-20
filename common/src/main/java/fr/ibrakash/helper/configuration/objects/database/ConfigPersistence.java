package fr.ibrakash.helper.configuration.objects.database;

import fr.ibrakash.helper.persistence.PersistenceType;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@ConfigSerializable
public class ConfigPersistence {

    private PersistenceType type = PersistenceType.JSON;
    private List<PersistenceType> fallbacks = new ArrayList<>(List.of(PersistenceType.SQL));
    private ConfigSql sql = new ConfigSql();
    private ConfigJsonStorage json = new ConfigJsonStorage();
    private ConfigMongo mongo = new ConfigMongo();

    public ConfigPersistence() {}

    public PersistenceType getType() {
        return type;
    }

    public List<PersistenceType> getFallbacks() {
        return fallbacks;
    }

    public ConfigSql getSql() {
        return sql;
    }

    public ConfigJsonStorage getJson() {
        return json;
    }

    public ConfigMongo getMongo() {
        return mongo;
    }

    public ConfigPersistence type(PersistenceType type) {
        this.type = type;
        return this;
    }

    public ConfigPersistence fallbacks(List<PersistenceType> fallbacks) {
        this.fallbacks = fallbacks == null ? new ArrayList<>() : new ArrayList<>(fallbacks);
        return this;
    }

    public ConfigPersistence sql(ConfigSql sql) {
        this.sql = sql;
        return this;
    }

    public ConfigPersistence json(ConfigJsonStorage json) {
        this.json = json;
        return this;
    }

    public ConfigPersistence mongo(ConfigMongo mongo) {
        this.mongo = mongo;
        return this;
    }

    public List<PersistenceType> orderedTypes() {
        LinkedHashSet<PersistenceType> order = new LinkedHashSet<>();
        order.add(this.type == null ? PersistenceType.JSON : this.type);
        if (this.fallbacks != null) {
            order.addAll(this.fallbacks);
        }
        return new ArrayList<>(order);
    }
}
