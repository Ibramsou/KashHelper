package fr.ibrakash.helper.configuration.objects.gui;

import fr.ibrakash.helper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.item.ConfigItems;
import fr.ibrakash.helper.configuration.objects.stream.ConfigFilter;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Arrays;
import java.util.List;

@ConfigSerializable
public class ConfigPagedGui extends AbstractGuiConfig {

    private ConfigGuiItem pagedItem = new ConfigGuiItem()
            .ingredientCharacter('X')
            .displayName("<gray>Example Paged Item");

    public ConfigPagedGui() {
        this.shape = ConfigGuiShapes.PAGED_SHAPE;
        this.items = List.of(
                ConfigItems.GLASS_PANE_DECORATION,
                ConfigItems.PREVIOUS_PAGE,
                ConfigItems.NEXT_PAGE
        );
    }

    public ConfigGuiItem getPagedItem() {
        return pagedItem;
    }
}
