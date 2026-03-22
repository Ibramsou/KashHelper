package fr.ibrakash.helper.example;

import fr.ibrakash.helper.configuration.ConfigurationLoaderType;
import fr.ibrakash.helper.configuration.ConfigReference;
import fr.ibrakash.helper.example.home.HomeRepository;
import fr.ibrakash.helper.example.sql.ExampleRepository;
import fr.ibrakash.helper.paper.KashPaperPlatform;
import fr.ibrakash.helper.paper.text.PaperTextReplacer;
import fr.ibrakash.helper.platform.KashAddon;
import fr.ibrakash.helper.platform.KashPlatform;
import fr.ibrakash.helper.platform.KashPlatformType;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ExamplePlugin extends JavaPlugin {

    private static ExamplePlugin instance;

    public static ExamplePlugin getInstance() {
        return instance;
    }

    private KashAddon<JavaPlugin> addon;
    private ExampleLocale exampleLocale;
    private ExampleItems exampleItems;
    private ExampleMenus exampleMenus;
    private ConfigReference<ExampleConfig> exampleConfig;
    private ConfigReference<ExamplePersistenceConfig> persistenceConfig;
    private HomeRepository homeRepository;
    private ExampleRepository exampleRepository;

    @Override
    public void onEnable() {
        instance = this;

        KashPlatform<JavaPlugin> platform = KashPlatform.get(KashPlatformType.PAPER, KashPaperPlatform.class);
        this.addon = platform.registerAddon(this);

        this.exampleConfig = this.addon.configurations().serializedConfig(
                ConfigurationLoaderType.YAML,
                ExampleConfig.class,
                () -> this.addon.paths().get("config", "yml")
        );

        this.persistenceConfig = this.addon.configurations().serializedConfig(
                ConfigurationLoaderType.YAML,
                ExamplePersistenceConfig.class,
                () -> this.addon.paths().get("persistence", "yml")
        );

        this.homeRepository = HomeRepository.create(this.addon, this.persistenceConfig.get().getPersistence());
        this.exampleRepository = new ExampleRepository(this.addon);

        this.exampleLocale = new ExampleLocale(this.addon);
        this.exampleMenus = new ExampleMenus(this.addon);
        this.exampleItems = new ExampleItems(this.addon);

        this.addon.configurations().reloadCachedMappers();

        ExampleConfig.get().getIntervals().forEach(exampleObject ->
                Bukkit.getScheduler().runTaskTimer(this, () -> {
                    this.exampleLocale.get(exampleObject.getLocalePath()).broadcast();
                    Bukkit.getOnlinePlayers().forEach(player ->
                            player.give(ExampleConfig.get().getReplaceableItem()
                                    .build(PaperTextReplacer.create().add("%player%", player.getName()))));
                }, 0L, exampleObject.getInterval()));

        PluginCommand exampleCommand = this.getCommand("example");
        if (exampleCommand != null) {
            exampleCommand.setExecutor(new ExampleCommand(this));
        }

        PluginCommand exampleDataCommand = this.getCommand("exampledata");
        if (exampleDataCommand != null) {
            exampleDataCommand.setExecutor(new ExampleDataCommand(this));
        }
    }

    public void reloadPersistenceDemo() {
        this.persistenceConfig.reload();
        if (this.homeRepository != null) {
            this.homeRepository.saveAll();
            this.homeRepository.close();
        }
        if (this.exampleRepository != null) {
            this.exampleRepository.saveAll();
            this.exampleRepository.close();
        }
        this.homeRepository = HomeRepository.create(this.addon, this.persistenceConfig.get().getPersistence());
        this.exampleRepository = new ExampleRepository(this.addon);
    }

    @Override
    public void onDisable() {
        if (this.homeRepository != null) {
            this.homeRepository.saveAll();
            this.homeRepository.close();
        }
        if (this.exampleRepository != null) {
            this.exampleRepository.saveAll();
            this.exampleRepository.close();
        }
    }

    public ExampleConfig getExampleConfig() {
        return this.exampleConfig.get();
    }

    public ExamplePersistenceConfig getPersistenceConfig() {
        return this.persistenceConfig.get();
    }

    public ExampleLocale getExampleLocale() {
        return this.exampleLocale;
    }

    public ExampleMenus getExampleMenus() {
        return this.exampleMenus;
    }

    public ExampleItems getExampleItems() {
        return exampleItems;
    }

    public KashAddon<JavaPlugin> addon() {
        return this.addon;
    }

    public HomeRepository getHomeRepository() {
        return homeRepository;
    }

    public ExampleRepository getExampleRepository() {
        return exampleRepository;
    }
}
