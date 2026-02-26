package fr.ibrakash.helper.example;

import fr.ibrakash.helper.configuration.ConfigurationLoaderType;
import fr.ibrakash.helper.configuration.ConfigurationObject;
import fr.ibrakash.helper.configuration.ConfigurationUtils;
import fr.ibrakash.helper.configuration.objects.gui.GuiConfig;
import fr.ibrakash.helper.configuration.objects.gui.PagedGuiConfig;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.utils.FileUtil;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class ExampleGuiConfig extends ConfigurationObject {

    private static ExampleGuiConfig instance;

    public static ExampleGuiConfig get() {
        return ConfigurationUtils.getUniqueInstance(
                ConfigurationLoaderType.YAML,
                instance,
                ExampleGuiConfig.class, () -> FileUtil.getPath(ExamplePlugin.getInstance(), "guis", "yml"),
                config -> instance = config);
    }

    private PagedGuiConfig pagedGui = new PagedGuiConfig()
            .pagedItem(
                    new ConfigGuiItem()
                            .ingredientCharacter('X')
                            .material("%material_name%")
                            .displayName("<gold>%material_name%")
                            .lore(List.of("<gray>Item #page_item_number"))
            );

    private GuiConfig normalGui = new GuiConfig()
            .title("<green>Normal Gui")
            .shape(
                    "# # # # # # # # #",
                    "# . . . X . . . #",
                    "# # # # # # # # #"
            ).items(
                    new ConfigGuiItem()
                            .ingredientCharacter('X')
                            .displayName("<red>Click Me")
                            .actions("click_me")
                            .lore(List.of("<green>Don't click me")),
                    new ConfigGuiItem()
                            .ingredientCharacter('#')
                            .displayName(" ")
                            .material(Material.GRAY_STAINED_GLASS_PANE)
            );

    public GuiConfig getNormalGui() {
        return normalGui;
    }

    public PagedGuiConfig getPagedGui() {
        return pagedGui;
    }
}
