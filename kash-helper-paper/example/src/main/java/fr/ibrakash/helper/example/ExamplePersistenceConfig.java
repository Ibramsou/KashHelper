package fr.ibrakash.helper.example;

import fr.ibrakash.helper.configuration.ConfigurationObject;
import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ExamplePersistenceConfig extends ConfigurationObject {

    private ConfigPersistence persistence = new ConfigPersistence();

    public ConfigPersistence getPersistence() {
        return persistence;
    }
}

