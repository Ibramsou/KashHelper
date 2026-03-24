package fr.ibrakash.helper.paper.serialization.itemstack;

import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializer;
import org.bukkit.inventory.ItemStack;

public class DataItemStackSerializer implements PersistedBlobSerializer<ItemStack> {

    @Override
    public byte[] serialize(ItemStack value) {
        return value == null ? new byte[0] : value.serializeAsBytes();
    }

    @Override
    public ItemStack deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        return ItemStack.deserializeBytes(bytes);
    }
}

