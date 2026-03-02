package fr.ibrakash.helper.item;

import fr.ibrakash.helper.utils.EnumUtil;
import fr.ibrakash.helper.utils.PlayerProfileCache;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Optional;
import java.util.UUID;

public class ItemUtil {

    @SuppressWarnings("UnstableApiUsage")
    public static ItemStack extraModelOnly(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return null;
        ItemMeta meta = itemStack.getItemMeta();
        ItemStack result = new ItemStack(itemStack.getType(), itemStack.getAmount());
        ItemMeta cloneMeta = result.getItemMeta();
        cloneMeta.setCustomModelDataComponent(meta.getCustomModelDataComponent());
        cloneMeta.displayName(meta.displayName());
        return result;
    }

    public static String extractName(ItemStack itemStack) {
        if (itemStack == null || itemStack.isEmpty()) return null;
        ItemMeta meta = itemStack.getItemMeta();
        Component component = meta.displayName();
        if (component == null) return EnumUtil.friendlyName(itemStack.getType());
        return MiniMessage.miniMessage().serialize(component);
    }

    public static Optional<ItemStack> parseSkullItem(String item) {
        if (item.contains(":")) {
            String[] split = item.split(":");
            String type =  split[0];
            String input = split[1];

            ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
            switch (type) {
                case "skin_texture" -> meta.setPlayerProfile(PlayerProfileCache.fetchTextureSkin(input));
                case "uuid_skin" -> meta.setPlayerProfile(PlayerProfileCache.fetchTextureSkin(UUID.fromString(input)).join());
                case "username_skin" -> meta.setPlayerProfile(PlayerProfileCache.fetchUsernameSkin(input).join());
                default -> {
                    return Optional.empty();
                }
            }

            itemStack.setItemMeta(meta);
            return Optional.of(itemStack);
        }

        return Optional.empty();
    }
}
