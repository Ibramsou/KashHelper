package fr.ibrakash.helper.gui.invui;

import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.item.ConfigItem;
import fr.ibrakash.helper.gui.GuiActionConsumer;
import fr.ibrakash.helper.gui.GuiItemWrapper;
import fr.ibrakash.helper.gui.GuiWrapper;
import fr.ibrakash.helper.text.TextReplacer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;

public class InvUiItem extends AbstractItem implements GuiItemWrapper {

    private final GuiWrapper<?, ?, ?> wrapper;
    private final ConfigGuiItem defaultItem;
    private ConfigGuiItem item;
    private GuiActionConsumer defaultConsumer;
    private TextReplacer customReplacers;

    public InvUiItem(GuiWrapper<?, ?, ?> wrapper, ConfigGuiItem item) {
        this.wrapper = wrapper;
        this.item = item;
        this.defaultItem = item;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(
                this.customReplacers == null ?
                        this.item.build(this.wrapper.replacer()) :
                        this.item.build(this.customReplacers)
        );
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (this.defaultConsumer != null) {
            this.defaultConsumer.doAction(player, event.getClick(), event, this);
        }
        ConfigGuiItem oldItem = this.item;
        this.wrapper.doActions(this.item, player, event, this);
        if (this.item != oldItem) this.updateItem();
    }

    @Override
    public ConfigGuiItem getDefaultItem() {
        return this.defaultItem;
    }

    @Override
    public void replaceItem(ConfigGuiItem item) {
        this.item = item;
    }

    @Override
    public ConfigGuiItem getConfigItem() {
        return this.item;
    }

    @Override
    public void updateItem() {
        this.notifyWindows();
    }

    public void setDefaultConsumer(GuiActionConsumer defaultConsumer) {
        this.defaultConsumer = defaultConsumer;
    }

    public void setCustomReplacers(TextReplacer customReplacers) {
        this.customReplacers = customReplacers;
    }
}
