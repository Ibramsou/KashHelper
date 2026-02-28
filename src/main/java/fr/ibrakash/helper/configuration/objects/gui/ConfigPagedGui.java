package fr.ibrakash.helper.configuration.objects.gui;

import fr.ibrakash.helper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.item.ConfigItems;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.Map;
import java.util.Optional;

@ConfigSerializable
public class ConfigPagedGui extends AbstractGuiConfig {

    private String pagedItem = "profile";

    public ConfigPagedGui() {
        this.shape = ConfigGuiShapes.PAGED_SHAPE;
        this.items = Map.of(
                "decoration", ConfigItems.GLASS_PANE_DECORATION,
                "previous-page", ConfigItems.PREVIOUS_PAGE,
                "next-page", ConfigItems.NEXT_PAGE
        );
    }

    public Optional<ConfigGuiItem> getPagedItem() {
        return this.getItem(this.pagedItem);
    }
}
