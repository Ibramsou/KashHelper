package fr.ibrakash.helper.platform;

import fr.ibrakash.helper.text.TextReplacer;
import fr.ibrakash.helper.text.TextUtil;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.util.function.Consumer;

public interface KashPlatform<V> {

    static KashPlatform<?> get() {
        return KashPlatformService.getPlatform();
    }

    static KashPlatform<?> get(KashPlatformType platformType) {
        return KashPlatformService.getPlatform(platformType);
    }

    static <P extends KashPlatform<?>> P get(KashPlatformType platformType, Class<P> platformClass) {
        return KashPlatformService.getPlatform(platformType, platformClass);
    }

    KashAddon<V> registerAddon(V addon);

    Consumer<TypeSerializerCollection.Builder> getConfigSerializer();

    TextUtil<?> textUtil();

    TextReplacer createTextReplacer();

    KashPlatformType platformType();

    int priority();
}
