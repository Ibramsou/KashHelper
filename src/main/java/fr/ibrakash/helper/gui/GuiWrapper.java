package fr.ibrakash.helper.gui;

import fr.ibrakash.helper.configuration.objects.gui.ConfigPagedGui;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.text.TextReplacer;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.*;

public abstract class GuiWrapper<G, C extends AbstractGuiConfig, W> {

    protected final C config;
    protected final Map<String, GuiAction> actions = new HashMap<>();
    protected final Map<Character, ConfigGuiItem> ingredients = new HashMap<>();
    protected final TextReplacer replacer = TextReplacer.create();

    protected G gui;
    protected W window;

    public GuiWrapper(C config) {
        this.config = config;
        if (this.config instanceof ConfigPagedGui configPagedGui) {
            this.ingredients.put(configPagedGui.getPagedItem().getShapeId(), configPagedGui.getPagedItem());
        }
        this.config.getItems().forEach(item -> this.ingredients.put(item.getShapeId(), item));
        this.ingredients.forEach((character, item) -> {
            String key = "switch_item:" + character;
            this.setAction(key, (issuer, type, event, wrapper) ->
                    wrapper.replaceItem(character == wrapper.getDefaultItem().getShapeId() ? wrapper.getDefaultItem() : item));
        });
        this.setAction("refresh", (issuer, type, event, item) -> this.refresh());
    }

    public TextReplacer replacer() {
        return replacer;
    }

    public void setAction(String key, GuiActionConsumer consumer) {
        this.actions.put(key, new GuiAction(key, consumer));
    }

    Optional<GuiAction> getAction(String key) {
        return Optional.ofNullable(this.actions.get(key));
    }

    public void doActions(ConfigGuiItem item, Player player, InventoryClickEvent event, GuiItemWrapper wrapper) {
        this.doActions(item.getActions(), player, event, wrapper);
    }

    public void doActions(List<String> actions, Player player, InventoryClickEvent event, GuiItemWrapper wrapper) {
        actions.forEach(key -> this.doAction(key, player, event, wrapper));
    }

    public void doAction(String key, Player player, InventoryClickEvent event, GuiItemWrapper wrapper) {
        this.getAction(key).ifPresent(guiAction -> guiAction.doAction(player, event, wrapper));
    }

    public void buildGui() {
        this.gui = this.build();
    }

    public void open(Player player) {
        if (this.gui == null) this.buildGui();
        this.window = this.createWindow(player);
    }

    public Component title() {
        return this.replacer.deserialize(this.config.getTitle());
    }

    protected abstract G build();

    protected abstract W createWindow(Player player);

    public abstract void refresh();

    public C getConfig() {
        return config;
    }
}
