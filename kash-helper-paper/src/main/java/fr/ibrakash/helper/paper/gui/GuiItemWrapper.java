package fr.ibrakash.helper.paper.gui;

import fr.ibrakash.helper.paper.configuration.objects.item.ConfigGuiItem;

public interface GuiItemWrapper {

    ConfigGuiItem getDefaultItem();

    void replaceItem(ConfigGuiItem item);

    ConfigGuiItem getConfigItem();

    void updateItem();
}
