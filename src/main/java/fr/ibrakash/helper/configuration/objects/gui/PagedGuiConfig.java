package fr.ibrakash.helper.configuration.objects.gui;

import fr.ibrakash.helper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class PagedGuiConfig extends AbstractGuiConfig {

    private ConfigGuiItem pagedItem = new ConfigGuiItem()
            .ingredientCharacter('X')
            .displayName("<gray>Example Paged Item");

    public PagedGuiConfig() {
        this.shape = List.of(
                "# # # # # # # # #",
                "# X X X X X X X #",
                "# X X X X X X X #",
                "# # P # # # N # #"
        );
        this.items = List.of(
                new ConfigGuiItem()
                        .ingredientCharacter('#')
                        .material(Material.GRAY_STAINED_GLASS_PANE)
                        .displayName(" "),
                new ConfigGuiItem()
                        .ingredientCharacter('P')
                        .material(Material.PAPER)
                        .displayName("<gray>Next Page")
                        .actions("next_page"),
                new ConfigGuiItem()
                        .ingredientCharacter('N')
                        .material(Material.PAPER)
                        .displayName("<gray>Previous Page")
                        .actions("previous_page")
        );
    }

    public PagedGuiConfig title(String title) {
        this.title = title;
        return this;
    }

    public PagedGuiConfig pagedItem(ConfigGuiItem item) {
        this.pagedItem = item;
        return this;
    }

    public PagedGuiConfig shape(String... shape) {
        this.shape = List.of(shape);
        return this;
    }

    public PagedGuiConfig items(ConfigGuiItem... items) {
        this.items = List.of(items);
        return this;
    }

    public ConfigGuiItem getPagedItem() {
        return pagedItem;
    }
}
