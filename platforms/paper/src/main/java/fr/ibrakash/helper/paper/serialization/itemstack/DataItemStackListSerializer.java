package fr.ibrakash.helper.paper.serialization.itemstack;

import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataItemStackListSerializer implements PersistedBlobSerializer<List<ItemStack>> {

    @Override
    public byte[] serialize(List<ItemStack> value) {
        if (value == null || value.isEmpty()) {
            return ItemStack.serializeItemsAsBytes(new ItemStack[0]);
        }
        return ItemStack.serializeItemsAsBytes(value.toArray(new ItemStack[0]));
    }

    @Override
    public List<ItemStack> deserialize(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return new ArrayList<>();
        }
        ItemStack[] items = ItemStack.deserializeItemsFromBytes(bytes);
        return new ArrayList<>(Arrays.asList(items));
    }

    @Override
    public List<ItemStack> defaultValue() {
        return new ArrayList<>();
    }
}

