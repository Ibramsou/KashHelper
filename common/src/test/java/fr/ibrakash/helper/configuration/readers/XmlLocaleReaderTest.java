package fr.ibrakash.helper.configuration.readers;

import fr.ibrakash.helper.platform.KashAddon;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class XmlLocaleReaderTest {

    @TempDir
    Path tempDir;

    @Test
    void resolvesMessageAndComponentPathsFromConstructorAutoLoad() {
        KashAddon<Object> addon = new KashAddon<>(new ResourceAnchor()) {
            @Override
            public File getAddonFolder() {
                return tempDir.toFile();
            }
        };

        XmlLocaleReader reader = new XmlLocaleReader(addon, "test-xml-locale");

        assertNotNull(reader.resolve("guild-info.container"));
        assertEquals("Guild Title", reader.resolve("guild-info.title"));
        assertEquals("Please enable your DMs.", reader.resolve("direct-message-not-allowed"));

        List<String> embedLines = reader.lines("guild-info.container");
        assertEquals("<container>", embedLines.getFirst());
        assertEquals("</container>", embedLines.getLast());
    }

    @Test
    void canDisableConstructorAutoLoadAndReloadManually() {
        KashAddon<Object> addon = new KashAddon<>(new ResourceAnchor()) {
            @Override
            public File getAddonFolder() {
                return tempDir.toFile();
            }
        };

        XmlLocaleReader reader = new XmlLocaleReader(addon, "test-xml-locale") {
            @Override
            protected boolean autoLoad() {
                return false;
            }
        };

        assertNull(reader.resolve("guild-info.title"));

        reader.reload();

        assertEquals("Guild Title", reader.resolve("guild-info.title"));
    }

    private static final class ResourceAnchor {
    }
}
