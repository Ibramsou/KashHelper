package fr.ibrakash.helper.example;

import fr.ibrakash.helper.configuration.ConfigurationUtils;
import fr.ibrakash.helper.text.TextReplacer;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin {

    private static ExamplePlugin instance;

    public static ExamplePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        ExampleConfig.get();
        ExampleLocale.get().reload(this);
        ExampleMenus.get().reload(this);
        ConfigurationUtils.reloadUniqueMappers();

        ExampleConfig.get().getIntervals().forEach(exampleObject -> {
            Bukkit.getScheduler().runTaskTimer(this, () -> {
                ExampleLocale.get().get(exampleObject.getLocalePath()).broadcast();
                Bukkit.getOnlinePlayers().forEach(player ->
                        player.give(ExampleConfig.get().getReplaceableItem().build(TextReplacer.create().add("%player%", player.getName()))));
            }, 0L, exampleObject.getInterval());
        });

        this.getCommand("example").setExecutor(new ExampleCommand());
    }
}
