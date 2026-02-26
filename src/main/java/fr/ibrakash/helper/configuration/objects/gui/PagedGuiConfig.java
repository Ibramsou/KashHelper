package fr.ibrakash.helper.configuration.objects.gui;

import fr.ibrakash.helper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.item.ConfigItems;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class PagedGuiConfig extends AbstractGuiConfig {

    private ConfigGuiItem pagedItem = new ConfigGuiItem()
            .ingredientCharacter('X')
            .displayName("<gray>Example Paged Item");

    public PagedGuiConfig() {
        this.shape = GuiValues.PAGED_SHAPE;
        this.items = List.of(
                ConfigItems.GLASS_PANE_DECORATION,
                ConfigItems.PREVIOUS_PAGE,
                ConfigItems.NEXT_PAGE
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
        return this.items(Arrays.asList(items));
    }

    public PagedGuiConfig items(List<ConfigGuiItem> items) {
        this.items = items;
        return this;
    }

    public ConfigGuiItem getPagedItem() {
        return pagedItem;
    }
}
