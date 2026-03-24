package fr.ibrakash.helper.paper.serialization.itemstack;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class JsonItemStackSerializer {

    private JsonItemStackSerializer() {
    }

    private static String encode(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    private static byte[] decode(String value) {
        if (value == null || value.isBlank()) {
            return new byte[0];
        }
        return Base64.getDecoder().decode(value);
    }

    public static class Serializer extends JsonSerializer<ItemStack> {
        @Override
        public void serialize(ItemStack value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            byte[] bytes = value == null ? new byte[0] : value.serializeAsBytes();
            gen.writeString(encode(bytes));
        }
    }

    public static class Deserializer extends JsonDeserializer<ItemStack> {
        @Override
        public ItemStack deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            byte[] bytes = decode(p.getValueAsString());
            if (bytes.length == 0) {
                return null;
            }
            return ItemStack.deserializeBytes(bytes);
        }
    }

    public static class ArraySerializer extends JsonSerializer<ItemStack[]> {
        @Override
        public void serialize(ItemStack[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            ItemStack[] items = value == null ? new ItemStack[0] : value;
            gen.writeString(encode(ItemStack.serializeItemsAsBytes(items)));
        }
    }

    public static class ArrayDeserializer extends JsonDeserializer<ItemStack[]> {
        @Override
        public ItemStack[] deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            byte[] bytes = decode(p.getValueAsString());
            if (bytes.length == 0) {
                return new ItemStack[0];
            }
            return ItemStack.deserializeItemsFromBytes(bytes);
        }
    }

    public static class ListSerializer extends JsonSerializer<List<ItemStack>> {
        @Override
        public void serialize(List<ItemStack> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            ItemStack[] items = value == null || value.isEmpty() ? new ItemStack[0] : value.toArray(new ItemStack[0]);
            gen.writeString(encode(ItemStack.serializeItemsAsBytes(items)));
        }
    }

    public static class ListDeserializer extends JsonDeserializer<List<ItemStack>> {
        @Override
        public List<ItemStack> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            byte[] bytes = decode(p.getValueAsString());
            if (bytes.length == 0) {
                return new ArrayList<>();
            }
            return new ArrayList<>(Arrays.asList(ItemStack.deserializeItemsFromBytes(bytes)));
        }
    }

    public static class SetSerializer extends JsonSerializer<Set<ItemStack>> {
        @Override
        public void serialize(Set<ItemStack> value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            ItemStack[] items = value == null || value.isEmpty() ? new ItemStack[0] : value.toArray(new ItemStack[0]);
            gen.writeString(encode(ItemStack.serializeItemsAsBytes(items)));
        }
    }

    public static class SetDeserializer extends JsonDeserializer<Set<ItemStack>> {
        @Override
        public Set<ItemStack> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            byte[] bytes = decode(p.getValueAsString());
            if (bytes.length == 0) {
                return new LinkedHashSet<>();
            }
            return new LinkedHashSet<>(Arrays.asList(ItemStack.deserializeItemsFromBytes(bytes)));
        }
    }
}

