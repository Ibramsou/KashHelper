package fr.ibrakash.helper.jda.configuration.readers;

import fr.ibrakash.helper.platform.KashAddon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdaEmbedConfigurationLocaleTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesComponentPathFromXmlLocale() {
        KashAddon<Object> addon = new KashAddon<>(new ResourceAnchor()) {
            @Override
            public File getAddonFolder() {
                return tempDir.toFile();
            }
        };

        JdaEmbedConfigurationLocale locale = new JdaEmbedConfigurationLocale(addon) {
            @Override
            public String key() {
                return "test-jda-locale";
            }
        };

        var serialized = locale.serialized("direct-message-not-allowed");
        var embedSerialized = locale.serialized("guild-info.container", "%guild_title%", "My Guild", "%score%", 87);
        var plainSerialized = locale.serialized("guild-summary", "%guild_title%", "My Guild", "%score%", 87);

        assertEquals("Please enable your DMs.", serialized.getContent());
        assertFalse(embedSerialized.getEmbeds().isEmpty());
        assertEquals("My Guild Infos", embedSerialized.getEmbeds().getFirst().getTitle());
        // <lines> produces plain text, not an embed
        assertTrue(plainSerialized.getEmbeds().isEmpty());
        assertTrue(plainSerialized.getContent().contains("Guild: My Guild"));
        assertTrue(plainSerialized.getContent().contains("Score: 87%"));
    }

    private static final class ResourceAnchor {
    }
}


