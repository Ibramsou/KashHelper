package fr.ibrakash.helper.configuration.objects.gui;

import fr.ibrakash.helper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public class GuiConfig extends AbstractGuiConfig {

    public GuiConfig() {}

    public GuiConfig title(String title) {
        this.title = title;
        return this;
    }

    public GuiConfig shape(String... shape) {
        this.shape = List.of(shape);
        return this;
    }

    public GuiConfig items(ConfigGuiItem... items) {
        this.items = List.of(items);
        return this;
    }
}
