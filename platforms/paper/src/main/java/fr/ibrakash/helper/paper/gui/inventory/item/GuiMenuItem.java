package fr.ibrakash.helper.paper.gui.inventory.item;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import fr.ibrakash.helper.paper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.paper.gui.GuiActionConsumer;
import fr.ibrakash.helper.paper.gui.GuiItemWrapper;
import fr.ibrakash.helper.paper.gui.GuiWrapper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class GuiMenuItem implements GuiItemWrapper {

    private static final Cache<UUID, Integer> RATE_LIMIT_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(2, TimeUnit.SECONDS)
            .build();

    private final GuiWrapper<?, ?, ?> wrapper;
    private final ConfigGuiItem defaultItem;
    private final Function<ConfigGuiItem, ItemStack> itemBuilder;

    private ConfigGuiItem item;
    private GuiActionConsumer defaultConsumer;
    private Consumer<GuiMenuItem> updateConsumer;

    public GuiMenuItem(GuiWrapper<?, ?, ?> wrapper, ConfigGuiItem item, Function<ConfigGuiItem, ItemStack> itemBuilder) {
        this.wrapper = wrapper;
        this.item = item;
        this.defaultItem = item;
        this.itemBuilder = itemBuilder;
    }

    public ItemStack buildItemStack() {
        return this.itemBuilder.apply(this.item);
    }

    public void handleClick(ClickType clickType, Player player, InventoryClickEvent event) {
        Integer lastClick = RATE_LIMIT_CACHE.getIfPresent(player.getUniqueId());
        if (lastClick != null && Bukkit.getCurrentTick() - lastClick < this.wrapper.getConfig().getClickRateLimit()) {
            event.setCancelled(true);
            return;
        }

        RATE_LIMIT_CACHE.put(player.getUniqueId(), Bukkit.getCurrentTick());

        if (this.defaultConsumer != null) {
            this.defaultConsumer.doAction(player, clickType, event, this);
        }
        ConfigGuiItem oldItem = this.item;
        this.wrapper.doActions(this.item, player, event, this);
        if (this.item != oldItem) {
            this.updateItem();
        }
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
        if (this.updateConsumer != null) {
            this.updateConsumer.accept(this);
        }
    }

    public void setDefaultConsumer(GuiActionConsumer defaultConsumer) {
        this.defaultConsumer = defaultConsumer;
    }

    public void setUpdateConsumer(Consumer<GuiMenuItem> updateConsumer) {
        this.updateConsumer = updateConsumer;
    }
}

