package fr.ibrakash.helper.paper;

import fr.ibrakash.helper.configuration.Configurations;
import fr.ibrakash.helper.paper.configuration.objects.action.ConfigGroupAction;
import fr.ibrakash.helper.paper.configuration.serializers.ActionSerializer;
import fr.ibrakash.helper.paper.configuration.serializers.SoundSerializer;
import fr.ibrakash.helper.paper.configuration.serializers.WorldSerializer;
import fr.ibrakash.helper.paper.text.PaperTextReplacer;
import fr.ibrakash.helper.paper.text.PaperTextUtil;
import fr.ibrakash.helper.platform.KashAddon;
import fr.ibrakash.helper.platform.KashPlatform;
import fr.ibrakash.helper.platform.KashPlatformType;
import fr.ibrakash.helper.text.TextReplacer;
import fr.ibrakash.helper.text.TextUtil;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.function.Consumer;

public class KashPaperPlatform implements KashPlatform<JavaPlugin> {

    private static final Consumer<TypeSerializerCollection.Builder> BUKKIT_SERIALIZERS = Configurations.composeSerializers(builder -> {
        builder.register(World.class, WorldSerializer.get());
        builder.register(Sound.class, SoundSerializer.get());
        builder.register(ConfigGroupAction.class, ActionSerializer.get());
    });

    private static final PaperTextUtil TEXT_UTIL = new PaperTextUtil();

    @Override
    public KashAddon<JavaPlugin> registerAddon(JavaPlugin addon) {
        return new KashPaperAddon(addon);
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
