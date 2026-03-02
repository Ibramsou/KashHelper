package fr.ibrakash.helper.gui;

import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.item.replacer.ItemReplacer;
import fr.ibrakash.helper.text.TextReplacer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

public abstract class GuiWrapper<G, C extends AbstractGuiConfig, W> {

    protected final C config;
    protected final Map<String, GuiActionConsumer> actions = new LinkedHashMap<>();
    protected final TextReplacer textReplacer = TextReplacer.create();
    protected final ItemReplacer itemReplacer = ItemReplacer.create();

    protected G gui;
    protected W window;

    public GuiWrapper(C config) {
        this.config = config;
        this.config.getItems().values().forEach(this::registerItemSwitcher);
        this.action("refresh", (issuer, type, event, item) -> this.refresh());
        this.action("close_inventory", (issuer, type, event, item) -> issuer.closeInventory());
    }

    private void registerItemSwitcher(ConfigGuiItem item) {
        String key = "switch_item:" + item.getId();
        this.action(key, (issuer, type, event, wrapper) ->
                wrapper.replaceItem(item.getId().equals(wrapper.getDefaultItem().getId()) ? wrapper.getDefaultItem() : item));
    }

    public TextReplacer replacer() {
        return textReplacer;
    }

    public ItemReplacer itemReplacer() {
        return itemReplacer;
    }
    public void action(String key, GuiActionConsumer consumer) {
        this.actions.put(key, consumer);
    }

    Optional<GuiActionConsumer> getAction(String key) {
        return Optional.ofNullable(this.actions.get(key));
    }

    public void doActions(ConfigGuiItem item, Player player, InventoryClickEvent event, GuiItemWrapper wrapper) {
        item.getActions().runActions(event.getClick(), execute ->
                this.doAction(execute, player, event, wrapper));
    }

    public void doAction(String key, Player player, InventoryClickEvent event, GuiItemWrapper wrapper) {
        this.getAction(key).ifPresent(guiAction -> guiAction.doAction(player, event.getClick(), event, wrapper));
    }

    public void buildGui() {
        this.gui = this.build();
    }

    public void open(Player player) {
        if (this.gui == null) this.buildGui();
        this.window = this.createWindow(player);
    }

    public abstract void close();

    public Component title() {
        return this.textReplacer.deserialize(this.config.getTitle());
    }

    protected abstract G build();

    protected abstract W createWindow(Player player);

    public abstract void refresh();

    public abstract void handleClose(boolean byPlayer);

    public C getConfig() {
        return config;
    }
}
