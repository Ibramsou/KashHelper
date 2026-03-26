package fr.ibrakash.helper.jda.configuration.readers;

import fr.ibrakash.helper.jda.configuration.objects.display.JdaConfigMessage;
import fr.ibrakash.helper.platform.KashAddon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JdaConfigurationLocaleTest {

    @TempDir
    Path tempDir;

    @Test
    void loadsMessagesAndLinesFromResourceBackedLocale() {
        KashAddon<Object> addon = new KashAddon<>(new ResourceAnchor()) {
            @Override
            public File getAddonFolder() {
                return tempDir.toFile();
            }
        };

        JdaConfigurationLocale locale = new JdaConfigurationLocale(addon) {
            @Override
            public String key() {
                return "test-jda-locale";
            }
        };

        JdaConfigMessage helloMessage = locale.message("hello");
        var serialized = locale.serialized("hello", "%name%", "Kash");
        var embedSerialized = locale.serialized("guild-info", "%guild_title%", "My Guild", "%score%", 87);

        assertNotNull(helloMessage);
        assertEquals("Hello Kash!", serialized.getContent());
        assertEquals(List.of("Hello %name%!"), locale.lines("hello"));
        assertFalse(embedSerialized.getEmbeds().isEmpty());
        assertEquals("My Guild Infos", embedSerialized.getEmbeds().getFirst().getTitle());
        assertEquals("Please enable your DMs.", locale.serialized("direct-message-not-allowed").getContent());
    }

    private static final class ResourceAnchor {
    }
}

