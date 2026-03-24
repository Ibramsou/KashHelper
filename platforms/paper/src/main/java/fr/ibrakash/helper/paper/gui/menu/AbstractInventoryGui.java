package fr.ibrakash.helper.paper.gui.menu;

import fr.ibrakash.helper.paper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.paper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.paper.gui.GuiWrapper;
import fr.ibrakash.helper.paper.gui.inventory.item.GuiMenuItem;
import fr.ibrakash.helper.paper.gui.inventory.layout.GuiLayout;
import fr.ibrakash.helper.paper.gui.inventory.window.InventoryWindow;
import fr.ibrakash.helper.platform.KashAddon;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractInventoryGui<C extends AbstractGuiConfig>
        extends GuiWrapper<GuiLayout, C, InventoryWindow> {

    protected final List<GuiMenuItem> items = new ArrayList<>();
    protected final Map<Integer, GuiMenuItem> staticSlotItems = new HashMap<>();

    private boolean closedByPlayer = true;

    protected AbstractInventoryGui(C config) {
        super(config);
    }

    @Override
    protected GuiLayout build() {
        this.items.clear();
        this.staticSlotItems.clear();

        GuiLayout layout = GuiLayout.fromShape(this.config.getShape());

        this.config.getItems().values().forEach(configGuiItem -> {
            if (!this.shouldUseAsStaticIngredient(configGuiItem)) {
                return;
            }
            List<Integer> slots = layout.slots(configGuiItem.getShapeCharacter());
            if (slots.isEmpty()) {
                return;
            }

            GuiMenuItem item = this.createItem(configGuiItem);
            this.items.add(item);
            slots.forEach(slot -> this.staticSlotItems.put(slot, item));
        });

        this.afterBuildLayout(layout);
        return layout;
    }

    @Override
    protected InventoryWindow createWindow(Player player, KashAddon<?> addon) {
        if (!(addon.getRaw() instanceof JavaPlugin plugin)) {
            throw new IllegalArgumentException("Addon raw instance must be a JavaPlugin to open Paper GUI");
        }

        InventoryWindow window = new InventoryWindow(
                addon,
                plugin,
                player,
                this.gui,
                this::title,
                this::buildSlotItems,
                () -> this.closedByPlayer,
                this::handleClose
        );
        window.open();
        return window;
    }

    @Override
    public void refresh() {
        if (this.window == null) {
            return;
        }
        this.window.redraw();
    }

    @Override
    public void close() {
        if (this.window == null) {
            return;
        }
        this.closedByPlayer = false;
        this.window.close();
        this.closedByPlayer = true;
    }

    protected GuiMenuItem createItem(ConfigGuiItem configItem) {
        return new GuiMenuItem(this, configItem, item -> item.build(this.textReplacer, this.itemReplacer));
    }

    protected boolean shouldUseAsStaticIngredient(ConfigGuiItem item) {
        return true;
    }

    protected void afterBuildLayout(GuiLayout layout) {}

    protected Map<Integer, GuiMenuItem> buildSlotItems() {
        return new HashMap<>(this.staticSlotItems);
    }
}
