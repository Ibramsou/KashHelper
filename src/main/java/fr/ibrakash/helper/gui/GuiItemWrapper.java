package fr.ibrakash.helper.gui;

import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;

public interface GuiItemWrapper {

    ConfigGuiItem getDefaultItem();

    void replaceItem(ConfigGuiItem item);

    ConfigGuiItem getConfigItem();

    void updateItem();
}
