package fr.ibrakash.helper.gui.invui;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.item.ConfigItem;
import fr.ibrakash.helper.gui.GuiActionConsumer;
import fr.ibrakash.helper.gui.GuiItemWrapper;
import fr.ibrakash.helper.gui.GuiWrapper;
import fr.ibrakash.helper.text.TextReplacer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class InvUiItem extends AbstractItem implements GuiItemWrapper {

    private static final Cache<UUID, Integer> RATE_LIMIT_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build();

    private final GuiWrapper<?, ?, ?> wrapper;
    private final ConfigGuiItem defaultItem;
    private ConfigGuiItem item;
    private GuiActionConsumer defaultConsumer;
    private final Function<ConfigGuiItem, ItemStack> itemBuilder;

    public InvUiItem(GuiWrapper<?, ?, ?> wrapper, ConfigGuiItem item, Function<ConfigGuiItem, ItemStack> itemBuilder) {
        this.wrapper = wrapper;
        this.item = item;
        this.defaultItem = item;
        this.itemBuilder = itemBuilder;
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(this.itemBuilder.apply(this.item));
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent event) {
        Integer lastClick = RATE_LIMIT_CACHE.getIfPresent(player.getUniqueId());
        if (lastClick != null && Bukkit.getCurrentTick() - lastClick < this.wrapper.getConfig().getClickRateLimit()) {
            event.setCancelled(true);
            return;
        }

        RATE_LIMIT_CACHE.put(player.getUniqueId(), Bukkit.getCurrentTick());

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
}
