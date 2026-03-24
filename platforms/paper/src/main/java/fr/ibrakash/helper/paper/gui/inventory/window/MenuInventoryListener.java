package fr.ibrakash.helper.paper.gui.inventory.window;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.plugin.Plugin;

final class MenuInventoryListener implements Listener {

    private final Plugin plugin;

    MenuInventoryListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        InventoryWindow.onInventoryClick(event);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryWindow.onInventoryClose(this.plugin, event);
    }
}

