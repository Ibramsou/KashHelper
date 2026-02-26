package fr.ibrakash.helper.gui;


import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public record GuiAction(String key, GuiActionConsumer consumer) {

    public void doAction(Player player, InventoryClickEvent event, GuiItemWrapper wrapper) {
        this.consumer.doAction(player, event.getClick(), event, wrapper);
        this.handleSwitch(wrapper);
    }

    private void handleSwitch(GuiItemWrapper wrapper) {
        Character switchId = GuiWrapper.ACTION_SWITCH_CACHE.get(this.key);
        if (switchId == null) {
            return;
        }

        wrapper.setReplacedItemId(switchId);
    }
}
