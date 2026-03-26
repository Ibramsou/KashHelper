package fr.ibrakash.helper.jda.platform;

import fr.ibrakash.helper.configuration.Configurations;
import fr.ibrakash.helper.jda.text.JdaTextReplacer;
import fr.ibrakash.helper.jda.text.JdaTextUtil;
import fr.ibrakash.helper.platform.KashAddon;
import fr.ibrakash.helper.platform.KashPlatform;
import fr.ibrakash.helper.platform.KashPlatformType;
import fr.ibrakash.helper.text.TextReplacer;
import fr.ibrakash.helper.text.TextUtil;
import net.dv8tion.jda.api.JDA;
import org.spongepowered.configurate.serialize.TypeSerializerCollection;

import java.io.File;
import java.util.function.Consumer;

public class KashJdaPlatform implements KashPlatform<JDA> {

    private static final JdaTextUtil TEXT_UTIL = new JdaTextUtil();

    @Override
    public KashAddon<JDA> registerAddon(JDA addon) {
        throw new UnsupportedOperationException(
                "JDA addons must be bootstrapped by constructing a KashJdaAddon subclass. " +
                "See KashJdaAddon for details."
        );
    }

    @Override
    public Consumer<TypeSerializerCollection.Builder> getConfigSerializer() {
        return Configurations.DEFAULT_SERIALIZERS;
    }

    @Override
    public TextUtil<?> textUtil() {
        return TEXT_UTIL;
    }

    @Override
    public TextReplacer createTextReplacer() {
        return new JdaTextReplacer();
    }

    @Override
    public KashPlatformType platformType() {
        return KashPlatformType.JDA;
    }

    @Override
    public int priority() {
        return 0;
    }
}

