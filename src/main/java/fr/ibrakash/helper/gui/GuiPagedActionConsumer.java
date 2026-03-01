package fr.ibrakash.helper.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

@FunctionalInterface
public interface GuiPagedActionConsumer<V> {

    void doAction(V object, Player issuer, ClickType type, InventoryClickEvent event, GuiItemWrapper item);
}
