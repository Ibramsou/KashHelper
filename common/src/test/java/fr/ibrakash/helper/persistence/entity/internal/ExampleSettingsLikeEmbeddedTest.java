package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.configuration.objects.database.ConfigSql;
import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import fr.ibrakash.helper.persistence.entity.PersistedEmbedded;
import fr.ibrakash.helper.persistence.entity.PersistedEntity;
import fr.ibrakash.helper.persistence.entity.PersistedId;
import fr.ibrakash.helper.persistence.sql.SqlPersistenceEngine;
import fr.ibrakash.helper.sql.SqlDriverType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that replicate exactly ExampleSettings behavior with inheritance.
 */
class ExampleSettingsLikeEmbeddedTest {

    @Test
    void exactExampleSettingsLike_withNonDefaultTheme_returnsNonNull() throws Exception {
        Path db = Files.createTempFile("kash-example-settings-like", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<ExampleDataLike, String> store = new SqlEntityStore<>(engine, ExampleDataLike.class, String.class);

            // Create with Java defaults (theme="default" which is non-null)
            ExampleDataLike data = new ExampleDataLike();
            data.id = "data-1";
            data.settings = new ExampleSettingsLike();
            // Java defaults: notifications=true, theme="default", x=0, y=0, z=0

            System.out.println("Before save: settings=" + data.settings);
            System.out.println("  notifications=" + data.settings.notifications);
            System.out.println("  theme=" + data.settings.theme);
            System.out.println("  x=" + data.settings.x);

            store.save(data);

            // Load and verify
            ExampleDataLike loaded = store.find("data-1").orElseThrow();
            System.out.println("After load: settings=" + loaded.settings);
            if (loaded.settings != null) {
                System.out.println("  notifications=" + loaded.settings.notifications);
                System.out.println("  theme=" + loaded.settings.theme);
                System.out.println("  x=" + loaded.settings.x);
            }

            assertNotNull(loaded.settings, "Settings should NOT be null because theme='default' is non-null");
            assertTrue(loaded.settings.notifications);
            assertEquals("default", loaded.settings.theme);
            assertEquals(0, loaded.settings.x);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void exactExampleSettingsLike_withNullThemeAndTrueNotify_returnsNull() throws Exception {
        Path db = Files.createTempFile("kash-example-settings-null-theme", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<ExampleDataLike, String> store = new SqlEntityStore<>(engine, ExampleDataLike.class, String.class);

            // Create with theme=null
            ExampleDataLike data = new ExampleDataLike();
            data.id = "data-2";
            data.settings = new ExampleSettingsLike();
            data.settings.theme = null;  // Override Java default
            data.settings.notifications = true;  // Matches annotation default
            // x, y, z = 0 (matches annotation defaults)

            store.save(data);

            // Load and verify - should be null because all values are default
            ExampleDataLike loaded = store.find("data-2").orElseThrow();
            assertNull(loaded.settings, "Settings should be null when all values are default");
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void exactExampleSettingsLike_withNonDefaultX_returnsNonNull() throws Exception {
        Path db = Files.createTempFile("kash-example-settings-nondefault-x", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<ExampleDataLike, String> store = new SqlEntityStore<>(engine, ExampleDataLike.class, String.class);

            // Create with non-default inherited x value
            ExampleDataLike data = new ExampleDataLike();
            data.id = "data-3";
            data.settings = new ExampleSettingsLike();
            data.settings.theme = null;  // null = default
            data.settings.notifications = true;  // matches default
            data.settings.x = 100;  // Non-default!

            store.save(data);

            // Load and verify
            ExampleDataLike loaded = store.find("data-3").orElseThrow();
            assertNotNull(loaded.settings, "Settings should NOT be null because x=100 is non-default");
            assertEquals(100, loaded.settings.x);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void exactExampleSettingsLike_settingsNull_loadsAsNull() throws Exception {
        Path db = Files.createTempFile("kash-example-settings-null", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<ExampleDataLike, String> store = new SqlEntityStore<>(engine, ExampleDataLike.class, String.class);

            // Create with settings=null
            ExampleDataLike data = new ExampleDataLike();
            data.id = "data-null";
            data.settings = null;

            store.save(data);

            // Load and verify
            ExampleDataLike loaded = store.find("data-null").orElseThrow();
            assertNull(loaded.settings, "Settings should remain null");
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void exactExampleSettingsLike_withCustomThemeGay_returnsNonNull() throws Exception {
        // This test replicates the exact user scenario: theme="gay" should NOT result in null settings
        Path db = Files.createTempFile("kash-example-settings-custom-theme", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<ExampleDataLike, String> store = new SqlEntityStore<>(engine, ExampleDataLike.class, String.class);

            // Create with custom theme value
            ExampleDataLike data = new ExampleDataLike();
            data.id = "data-gay";
            data.settings = new ExampleSettingsLike();
            data.settings.theme = "gay";  // Non-default, non-null value
            data.settings.notifications = true;  // Matches annotation default
            // x, y, z = 0 (matches annotation defaults)

            System.out.println("=== TEST: theme=gay ===");
            System.out.println("Before save: settings=" + data.settings);
            System.out.println("  theme=" + data.settings.theme);

            store.save(data);

            // Load and verify - settings should NOT be null because theme="gay"
            ExampleDataLike loaded = store.find("data-gay").orElseThrow();
            System.out.println("After load: settings=" + loaded.settings);
            if (loaded.settings != null) {
                System.out.println("  theme=" + loaded.settings.theme);
                System.out.println("  notifications=" + loaded.settings.notifications);
            } else {
                System.out.println("  ERROR: settings is null!");
            }

            assertNotNull(loaded.settings, "Settings should NOT be null because theme='gay' is non-default");
            assertEquals("gay", loaded.settings.theme);
            assertTrue(loaded.settings.notifications);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    // -------------------- Test entities that mirror ExampleData/ExampleSettings --------------------

    @PersistedEntity("example_data_like")
    static class ExampleDataLike {
        @PersistedId("id")
        String id;

        @PersistedEmbedded(prefix = "settings_", nullable = true)
        ExampleSettingsLike settings;
    }

    // Mirrors ExampleDisplayAnchor
    static class ExampleDisplayAnchorLike {
        @PersistedColumn(value = "x", nullable = false, defaultValue = "0")
        int x;

        @PersistedColumn(value = "y", nullable = false, defaultValue = "0")
        int y;

        @PersistedColumn(value = "z", nullable = false, defaultValue = "0")
        int z;
    }

    // Mirrors ExampleSettings
    static class ExampleSettingsLike extends ExampleDisplayAnchorLike {
        @PersistedColumn(value = "notify", nullable = false, defaultValue = "true")
        boolean notifications = true;

        // No defaultValue annotation, has Java default
        @PersistedColumn(value = "theme", length = 24)
        String theme = "default";
    }
}


