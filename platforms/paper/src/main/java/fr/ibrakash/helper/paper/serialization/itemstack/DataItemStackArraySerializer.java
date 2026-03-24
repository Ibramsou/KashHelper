package fr.ibrakash.helper.paper.serialization.itemstack;

import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializer;
import org.bukkit.inventory.ItemStack;

public class DataItemStackArraySerializer implements PersistedBlobSerializer<ItemStack[]> {

    @Override
    public byte[] serialize(ItemStack[] value) {
        ItemStack[] items = value == null ? new ItemStack[0] : value;
        return ItemStack.serializeItemsAsBytes(items);
    }

    @Override
    public ItemStack[] deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new ItemStack[0];
        }
        return ItemStack.deserializeItemsFromBytes(bytes);
    }

    @Override
    public ItemStack[] defaultValue() {
        return new ItemStack[0];
    }
}

