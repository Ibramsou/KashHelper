package fr.ibrakash.helper.paper.gui.inventory.window;

import fr.ibrakash.helper.paper.KashPaperAddon;
import fr.ibrakash.helper.paper.gui.inventory.item.GuiMenuItem;
import fr.ibrakash.helper.paper.gui.inventory.layout.GuiLayout;
import fr.ibrakash.helper.paper.utils.QuickScheduler;
import fr.ibrakash.helper.platform.KashAddon;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class InventoryWindow {

    private static final Map<Inventory, InventoryWindow> OPEN_WINDOWS = new ConcurrentHashMap<>();
    private static final Map<UUID, InventoryWindow> OPEN_WINDOWS_BY_VIEWER = new ConcurrentHashMap<>();
    private static final Set<Plugin> REGISTERED_PLUGINS = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> PENDING_CLOSE_TOKENS = new ConcurrentHashMap<>();
    private static final AtomicLong CLOSE_TOKEN_SEQUENCE = new AtomicLong();

    private final Player viewer;
    private final GuiLayout layout;
    private final Supplier<Component> titleSupplier;
    private final Supplier<Map<Integer, GuiMenuItem>> slotSupplier;
    private final BooleanSupplier byPlayerCloseSupplier;
    private final Consumer<Boolean> closeConsumer;
    private final QuickScheduler scheduler;

    private Inventory inventory;
    private Map<Integer, GuiMenuItem> slotItems = new HashMap<>();
    private boolean ignoreNextClose;

    public InventoryWindow(
            KashAddon<?> addon,
            JavaPlugin plugin,
            Player viewer,
            GuiLayout layout,
            Supplier<Component> titleSupplier,
            Supplier<Map<Integer, GuiMenuItem>> slotSupplier,
            BooleanSupplier byPlayerCloseSupplier,
            Consumer<Boolean> closeConsumer
    ) {
        this.scheduler = new QuickScheduler(plugin);
        this.viewer = viewer;
        this.layout = layout;
        this.titleSupplier = titleSupplier;
        this.slotSupplier = slotSupplier;
        this.byPlayerCloseSupplier = byPlayerCloseSupplier;
        this.closeConsumer = closeConsumer;

        ensureListenerRegistered(addon, plugin);
    }

    public void open() {
        this.redraw();
    }

    public void redraw() {
        if (!Bukkit.isPrimaryThread()) {
            this.scheduler.runPlayerTask(this.viewer, this::redraw);
            return;
        }

        Map<Integer, GuiMenuItem> newSlotItems = this.slotSupplier.get();
        Inventory newInventory = Bukkit.createInventory(new WindowHolder(this), this.layout.size(), this.titleSupplier.get());

        newSlotItems.forEach((slot, item) -> {
            if (slot < 0 || slot >= newInventory.getSize()) {
                return;
            }
            newInventory.setItem(slot, item.buildItemStack());
            item.setUpdateConsumer(this::updateItem);
        });

        Inventory oldInventory = this.inventory;
        this.inventory = newInventory;
        this.slotItems = newSlotItems;
        OPEN_WINDOWS.put(newInventory, this);
        OPEN_WINDOWS_BY_VIEWER.put(this.viewer.getUniqueId(), this);
        cancelPendingClose(this.viewer.getUniqueId());

        if (oldInventory != null) {
            this.ignoreNextClose = true;
        }

        this.viewer.openInventory(newInventory);
    }

    public void close() {
        if (!Bukkit.isPrimaryThread()) {
            this.scheduler.runPlayerTask(this.viewer, this::close);
            return;
        }
        this.viewer.closeInventory();
    }

    private void updateItem(GuiMenuItem item) {
        if (!Bukkit.isPrimaryThread()) {
            this.scheduler.runPlayerTask(this.viewer, () -> this.updateItem(item));
            return;
        }
        if (this.inventory == null) {
            return;
        }
        this.slotItems.forEach((slot, slotItem) -> {
            if (slotItem != item || slot < 0 || slot >= this.inventory.getSize()) {
                return;
            }
            this.inventory.setItem(slot, item.buildItemStack());
        });
        this.viewer.updateInventory();
    }

    private void handleClick(InventoryClickEvent event) {
        if (this.inventory == null || event.getRawSlot() < 0 || event.getRawSlot() >= this.inventory.getSize()) {
            return;
        }
        event.setCancelled(true);
        GuiMenuItem item = this.slotItems.get(event.getRawSlot());
        if (item == null) {
            return;
        }
        item.handleClick(event.getClick(), this.viewer, event);
    }

    private void handleClose() {
        if (this.ignoreNextClose) {
            this.ignoreNextClose = false;
            return;
        }
        this.closeConsumer.accept(this.byPlayerCloseSupplier.getAsBoolean());
    }

    private static void ensureListenerRegistered(KashAddon<?> addon, Plugin plugin) {
        if (addon instanceof KashPaperAddon paperAddon) {
            if (!paperAddon.markGuiListenerRegistered()) {
                return;
            }
        } else if (!REGISTERED_PLUGINS.add(plugin)) {
            return;
        }

        Bukkit.getPluginManager().registerEvents(new MenuInventoryListener(), plugin);
    }

    static void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryWindow window = OPEN_WINDOWS.get(top);
        if (window == null) {
            window = fromHolder(top);
            if (window != null) {
                OPEN_WINDOWS.putIfAbsent(top, window);
            }
        }

        // Never intercept clicks when the active top inventory is not this managed GUI.
        if (window == null || !window.isManagedInventory(top)) {
            return;
        }

        cancelPendingClose(event.getWhoClicked().getUniqueId());
        window.handleClick(event);
    }

    static void onInventoryClose(InventoryCloseEvent event) {
        Inventory closedInventory = event.getInventory();
        InventoryWindow window = OPEN_WINDOWS.get(closedInventory);
        if (window == null) {
            window = fromHolder(closedInventory);
        }
        if (window == null) {
            window = OPEN_WINDOWS_BY_VIEWER.get(event.getPlayer().getUniqueId());
        }
        if (window == null) {
            return;
        }

        if (window.ignoreNextClose) {
            window.ignoreNextClose = false;
            return;
        }

        // Dialog/open-new transitions should not detach the current GUI window.
        if (event.getReason() == InventoryCloseEvent.Reason.OPEN_NEW) {
            OPEN_WINDOWS_BY_VIEWER.put(event.getPlayer().getUniqueId(), window);
            return;
        }

        scheduleCloseValidation(event.getPlayer().getUniqueId(), window, 8);
    }

    private static void scheduleCloseValidation(UUID viewerId, InventoryWindow target, int retries) {
        cancelPendingClose(viewerId);
        long token = CLOSE_TOKEN_SEQUENCE.incrementAndGet();
        PENDING_CLOSE_TOKENS.put(viewerId, token);
        scheduleCloseValidation(viewerId, target, retries, token);
    }

    private static void scheduleCloseValidation(UUID viewerId, InventoryWindow target, int retries, long token) {
        target.scheduler.runPlayerTask(target.viewer, () -> {
            if (!isCloseTokenActive(viewerId, token)) {
                return;
            }

            if (!target.viewer.isOnline()) {
                clearCloseToken(viewerId, token);
                cleanupWindow(viewerId, target);
                return;
            }

            Inventory currentTop = target.viewer.getOpenInventory().getTopInventory();
            if (target.isManagedInventory(currentTop)) {
                OPEN_WINDOWS.putIfAbsent(currentTop, target);
                OPEN_WINDOWS_BY_VIEWER.put(viewerId, target);
                clearCloseToken(viewerId, token);
                return;
            }

            if (retries > 1) {
                scheduleCloseValidation(viewerId, target, retries - 1, token);
                return;
            }

            clearCloseToken(viewerId, token);
            cleanupWindow(viewerId, target);
            target.handleClose();
        });
    }

    private static void cleanupWindow(UUID viewerId, InventoryWindow target) {
        OPEN_WINDOWS.entrySet().removeIf(entry -> entry.getValue() == target);
        OPEN_WINDOWS_BY_VIEWER.remove(viewerId, target);
    }

    private static void cancelPendingClose(UUID viewerId) {
        PENDING_CLOSE_TOKENS.remove(viewerId);
    }

    private static boolean isCloseTokenActive(UUID viewerId, long token) {
        Long activeToken = PENDING_CLOSE_TOKENS.get(viewerId);
        return activeToken != null && activeToken == token;
    }

    private static void clearCloseToken(UUID viewerId, long token) {
        PENDING_CLOSE_TOKENS.remove(viewerId, token);
    }

    private boolean isManagedInventory(Inventory inventory) {
        if (inventory == null) {
            return false;
        }
        return inventory == this.inventory || fromHolder(inventory) == this;
    }

    private static InventoryWindow fromHolder(Inventory inventory) {
        if (inventory == null) {
            return null;
        }
        InventoryHolder holder = inventory.getHolder(false);
        if (holder == null) {
            holder = inventory.getHolder();
        }
        if (holder instanceof WindowHolder windowHolder) {
            return windowHolder.window();
        }
        return null;
    }

    private record WindowHolder(InventoryWindow window) implements InventoryHolder {
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
