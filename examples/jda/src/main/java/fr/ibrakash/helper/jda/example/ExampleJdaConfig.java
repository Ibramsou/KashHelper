package fr.ibrakash.helper.jda.example;

import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.jda.platform.KashJdaConfig;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

@ConfigSerializable
public class ExampleJdaConfig extends KashJdaConfig {

    private ConfigPersistence database = new ConfigPersistence();

    public static ExampleJdaConfig get() {
        return JdaExample.getInstance().getConfig();
    }

    @Override
    public String defaultToken() {
        return "YOUR_BOT_TOKEN";
    }

    @Override
    public String defaultDevGuildId() {
        return "1479442680470835222";
    }

    public ConfigPersistence getDatabase() {
        return database;
    }
}
