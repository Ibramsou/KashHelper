package fr.ibrakash.helper.gui;


import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public record GuiAction(String key, GuiActionConsumer consumer) {

    public void doAction(Player player, InventoryClickEvent event, GuiItemWrapper wrapper) {
        this.consumer.doAction(player, event.getClick(), event, wrapper);
    }
}
