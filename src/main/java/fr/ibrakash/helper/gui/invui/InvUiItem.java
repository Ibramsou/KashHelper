package fr.ibrakash.helper.gui.invui;

import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.gui.GuiActionConsumer;
import fr.ibrakash.helper.gui.GuiItemWrapper;
import fr.ibrakash.helper.gui.GuiWrapper;
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
    private final ConfigGuiItem item;
    private char switchedId;
    private GuiActionConsumer defaultConsumer;
    private List<Object> customReplacers;

    public InvUiItem(GuiWrapper<?, ?, ?> wrapper, ConfigGuiItem item) {
        this.wrapper = wrapper;
        this.item = item;
    }

    @Override
    public ItemProvider getItemProvider() {
        return this.wrapper.getSwitchedItem(this)
                .map(ItemBuilder::new)
                .orElse(new ItemBuilder(
                        this.customReplacers == null ?
                                this.item.build(this.wrapper.getReplacers()) :
                                this.item.build(this.customReplacers)
                ));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        if (this.defaultConsumer != null) {
            this.defaultConsumer.doAction(player, event.getClick(), event, this);
        }
        this.wrapper.doActions(this.item, player, event, this);
    }

    @Override
    public void setReplacedItemId(char ingredientId) {
        this.switchedId = ingredientId;
    }

    @Override
    public char getReplacedItemId() {
        return this.switchedId;
    }

    @Override
    public void updateItem() {
        this.notifyWindows();
    }

    public void setDefaultConsumer(GuiActionConsumer defaultConsumer) {
        this.defaultConsumer = defaultConsumer;
    }

    public void setCustomReplacers(List<Object> customReplacers) {
        this.customReplacers = customReplacers;
    }
}
