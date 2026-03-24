package fr.ibrakash.helper.paper.serialization.itemstack;

import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

public class DataItemStackSetSerializer implements PersistedBlobSerializer<Set<ItemStack>> {

    @Override
    public byte[] serialize(Set<ItemStack> value) {
        if (value == null || value.isEmpty()) {
            return ItemStack.serializeItemsAsBytes(new ItemStack[0]);
        }
        return ItemStack.serializeItemsAsBytes(value.toArray(new ItemStack[0]));
    }

    @Override
    public Set<ItemStack> deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new LinkedHashSet<>();
        }
        ItemStack[] items = ItemStack.deserializeItemsFromBytes(bytes);
        return new LinkedHashSet<>(Arrays.asList(items));
    }

    @Override
    public Set<ItemStack> defaultValue() {
        return new LinkedHashSet<>();
    }
}

