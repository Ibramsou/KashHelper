package fr.ibrakash.helper.paper;

import fr.ibrakash.helper.configuration.Configurations;
import fr.ibrakash.helper.persistence.entity.PersistedBlobSerializerRegistry;
import fr.ibrakash.helper.paper.chunk.entity.EntityChunkTrackingCache;
import fr.ibrakash.helper.paper.configuration.serializers.SoundSerializer;
import fr.ibrakash.helper.paper.configuration.serializers.WorldSerializer;
import fr.ibrakash.helper.paper.serialization.itemstack.DataItemStackArraySerializer;
import fr.ibrakash.helper.paper.serialization.itemstack.DataItemStackListSerializer;
import fr.ibrakash.helper.paper.serialization.itemstack.DataItemStackSerializer;
import fr.ibrakash.helper.paper.serialization.itemstack.DataItemStackSetSerializer;
import fr.ibrakash.helper.paper.text.PaperTextReplacer;
import fr.ibrakash.helper.paper.text.PaperTextUtil;
import fr.ibrakash.helper.platform.KashAddon;
import fr.ibrakash.helper.platform.KashPlatform;
import fr.ibrakash.helper.platform.KashPlatformType;
import fr.ibrakash.helper.text.TextReplacer;
import fr.ibrakash.helper.text.TextUtil;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class KashPaperPlatform implements KashPlatform<JavaPlugin> {

    static {
        PersistedBlobSerializerRegistry.register(ItemStack.class, DataItemStackSerializer::new);
        PersistedBlobSerializerRegistry.register(ItemStack[].class, DataItemStackArraySerializer::new);
        PersistedBlobSerializerRegistry.register(PersistedBlobSerializerRegistry.parameterizedType(List.class, ItemStack.class), DataItemStackListSerializer::new);
        PersistedBlobSerializerRegistry.register(PersistedBlobSerializerRegistry.parameterizedType(Set.class, ItemStack.class), DataItemStackSetSerializer::new);
    }

    private static final Consumer<TypeSerializerCollection.Builder> BUKKIT_SERIALIZERS = Configurations.createSerializer(builder -> {
        builder.register(World.class, WorldSerializer.get());
        builder.register(Sound.class, SoundSerializer.get());
    });

    private static final PaperTextUtil TEXT_UTIL = new PaperTextUtil();

    @Override
    public KashAddon<JavaPlugin> registerAddon(JavaPlugin addon) {
        KashPaperAddon paperAddon = new KashPaperAddon(addon);
        EntityChunkTrackingCache.register(paperAddon);
        return paperAddon;
    }

    @Override
    public Consumer<TypeSerializerCollection.Builder> getConfigSerializer() {
        return BUKKIT_SERIALIZERS;
    }

    @Override
    public TextUtil<?> textUtil() {
        return TEXT_UTIL;
    }

    @Override
    public TextReplacer createTextReplacer() {
        return new PaperTextReplacer();
    }

    @Override
    public KashPlatformType platformType() {
        return KashPlatformType.PAPER;
    }

    @Override
    public int priority() {
        return 0;
    }
}
