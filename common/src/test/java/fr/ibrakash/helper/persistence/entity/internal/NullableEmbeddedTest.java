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
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for @PersistedEmbedded(nullable = true) functionality.
 *
 * When an embedded is marked as nullable:
 * - If ALL columns have default values → embedded should be null after deserialization
 * - If ANY column has a non-default value → embedded should NOT be null
 */
class NullableEmbeddedTest {

    @Test
    void nullableEmbedded_allDefaultValues_returnsNull() throws Exception {
        Path db = Files.createTempFile("kash-nullable-embedded-all-default", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<TownWarData, String> store = new SqlEntityStore<>(engine, TownWarData.class, String.class);

            // Save entity with all-default embedded values
            TownWarData town = new TownWarData();
            town.id = "town-1";
            town.name = "TestTown";
            town.shield = new ShieldDisplay(); // All fields are default: x=0, y=0, z=0, level=0

            store.save(town);

            // Load and verify
            TownWarData loaded = store.find("town-1").orElseThrow();
            assertEquals("TestTown", loaded.name);
            assertNull(loaded.shield, "Nullable embedded with all-default values should be null");
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nullableEmbedded_oneNonDefaultValue_returnsNonNull() throws Exception {
        Path db = Files.createTempFile("kash-nullable-embedded-non-default", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<TownWarData, String> store = new SqlEntityStore<>(engine, TownWarData.class, String.class);

            // Save entity with one non-default value
            TownWarData town = new TownWarData();
            town.id = "town-2";
            town.name = "TestTown2";
            town.shield = new ShieldDisplay();
            town.shield.level = 3; // Non-default value

            store.save(town);

            // Load and verify
            TownWarData loaded = store.find("town-2").orElseThrow();
            assertEquals("TestTown2", loaded.name);
            assertNotNull(loaded.shield, "Nullable embedded with non-default value should NOT be null");
            assertEquals(3, loaded.shield.level);
            assertEquals(0, loaded.shield.x);
            assertEquals(0, loaded.shield.y);
            assertEquals(0, loaded.shield.z);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nullableEmbedded_inheritedNonDefaultValue_returnsNonNull() throws Exception {
        Path db = Files.createTempFile("kash-nullable-embedded-inherited", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<TownWarData, String> store = new SqlEntityStore<>(engine, TownWarData.class, String.class);

            // Save entity with non-default value in inherited field
            TownWarData town = new TownWarData();
            town.id = "town-3";
            town.name = "TestTown3";
            town.shield = new ShieldDisplay();
            town.shield.x = 100; // Non-default value in inherited field
            town.shield.y = 64;
            town.shield.z = -50;

            store.save(town);

            // Load and verify
            TownWarData loaded = store.find("town-3").orElseThrow();
            assertEquals("TestTown3", loaded.name);
            assertNotNull(loaded.shield, "Nullable embedded with non-default inherited value should NOT be null");
            assertEquals(0, loaded.shield.level);
            assertEquals(100, loaded.shield.x);
            assertEquals(64, loaded.shield.y);
            assertEquals(-50, loaded.shield.z);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nullableEmbedded_withAnnotatedDefault_matchingValueReturnsNull() throws Exception {
        Path db = Files.createTempFile("kash-nullable-embedded-annotated-default", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<ProfileWithSettings, String> store = new SqlEntityStore<>(engine, ProfileWithSettings.class, String.class);

            // Save entity with values that match annotated defaults
            ProfileWithSettings profile = new ProfileWithSettings();
            profile.id = "profile-1";
            profile.settings = new SettingsWithDefaults();
            profile.settings.notifications = true;  // Matches @PersistedColumn defaultValue="true"
            profile.settings.volume = 50;           // Matches @PersistedColumn defaultValue="50"

            store.save(profile);

            // Load and verify
            ProfileWithSettings loaded = store.find("profile-1").orElseThrow();
            assertNull(loaded.settings, "Nullable embedded with all values matching annotated defaults should be null");
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nullableEmbedded_withAnnotatedDefault_differentValueReturnsNonNull() throws Exception {
        Path db = Files.createTempFile("kash-nullable-embedded-annotated-non-default", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<ProfileWithSettings, String> store = new SqlEntityStore<>(engine, ProfileWithSettings.class, String.class);

            // Save entity with value different from annotated default
            ProfileWithSettings profile = new ProfileWithSettings();
            profile.id = "profile-2";
            profile.settings = new SettingsWithDefaults();
            profile.settings.notifications = false; // Different from defaultValue="true"
            profile.settings.volume = 50;           // Matches defaultValue="50"

            store.save(profile);

            // Load and verify
            ProfileWithSettings loaded = store.find("profile-2").orElseThrow();
            assertNotNull(loaded.settings, "Nullable embedded with non-default value should NOT be null");
            assertFalse(loaded.settings.notifications);
            assertEquals(50, loaded.settings.volume);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nonNullableEmbedded_allDefaultValues_stillInstantiated() throws Exception {
        Path db = Files.createTempFile("kash-non-nullable-embedded", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<TownWithNonNullableShield, String> store = new SqlEntityStore<>(engine, TownWithNonNullableShield.class, String.class);

            // Save entity with all-default embedded values
            TownWithNonNullableShield town = new TownWithNonNullableShield();
            town.id = "town-nn-1";
            town.name = "NonNullableTown";
            town.shield = new ShieldDisplay(); // All fields are default

            store.save(town);

            // Load and verify - non-nullable embedded should always be instantiated
            TownWithNonNullableShield loaded = store.find("town-nn-1").orElseThrow();
            assertEquals("NonNullableTown", loaded.name);
            assertNotNull(loaded.shield, "Non-nullable embedded should always be instantiated");
            assertEquals(0, loaded.shield.level);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nullableEmbedded_savedAsNull_loadsAsNull() throws Exception {
        Path db = Files.createTempFile("kash-nullable-embedded-saved-null", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<TownWarData, String> store = new SqlEntityStore<>(engine, TownWarData.class, String.class);

            // Save entity with null embedded
            TownWarData town = new TownWarData();
            town.id = "town-null";
            town.name = "NullShieldTown";
            town.shield = null;

            store.save(town);

            // Load and verify
            TownWarData loaded = store.find("town-null").orElseThrow();
            assertEquals("NullShieldTown", loaded.name);
            assertNull(loaded.shield, "Null embedded should remain null");
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nullableEmbedded_withStringFieldAndJavaDefault_returnsNonNull() throws Exception {
        // This test replicates ExampleSettings behavior where:
        // - theme has a Java default "default" but no annotation defaultValue
        // - notifications has both Java default true and annotation defaultValue="true"
        Path db = Files.createTempFile("kash-nullable-embedded-string-java-default", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<EntityWithComplexSettings, String> store = new SqlEntityStore<>(engine, EntityWithComplexSettings.class, String.class);

            // Save entity with Java defaults (theme="default" which is non-null)
            EntityWithComplexSettings entity = new EntityWithComplexSettings();
            entity.id = "entity-1";
            entity.settings = new ComplexSettings(); // Uses Java defaults: notify=true, theme="default"

            store.save(entity);

            // Load and verify - should NOT be null because theme="default" is non-null
            EntityWithComplexSettings loaded = store.find("entity-1").orElseThrow();
            assertNotNull(loaded.settings, "Embedded with non-null String field should NOT be null");
            assertTrue(loaded.settings.notify);
            assertEquals("default", loaded.settings.theme);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nullableEmbedded_withAllNullableStringsNull_returnsNull() throws Exception {
        Path db = Files.createTempFile("kash-nullable-embedded-all-strings-null", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<EntityWithComplexSettings, String> store = new SqlEntityStore<>(engine, EntityWithComplexSettings.class, String.class);

            // Save entity where all values match defaults (including annotated ones)
            EntityWithComplexSettings entity = new EntityWithComplexSettings();
            entity.id = "entity-2";
            entity.settings = new ComplexSettings();
            entity.settings.notify = true;  // Matches annotation default
            entity.settings.theme = null;   // null String should be considered default

            store.save(entity);

            // Load and verify - should be null because all values are default
            EntityWithComplexSettings loaded = store.find("entity-2").orElseThrow();
            assertNull(loaded.settings, "Embedded with all default values should be null");
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nullableEmbedded_partialProjectionMustNotForceNull() throws Exception {
        Path db = Files.createTempFile("kash-nullable-embedded-partial", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<TownWarData, String> store = new SqlEntityStore<>(engine, TownWarData.class, String.class);

            TownWarData town = new TownWarData();
            town.id = "town-partial";
            town.name = "PartialTown";
            town.shield = new ShieldDisplay();
            town.shield.x = 10;   // non-default in DB
            town.shield.level = 0; // default value

            store.save(town);

            // Load only a partial projection including one embedded default column.
            // Before the fix, nullable evaluation could nullify shield based on incomplete data.
            List<TownWarData> loaded = store.findAllByIds(List.of("town-partial"), Set.of("name", "shield_level"));

            assertEquals(1, loaded.size());
            assertNotNull(loaded.get(0).shield, "Partial projection must not force nullable embedded to null");
        } finally {
            Files.deleteIfExists(db);
        }
    }

    // -------------------- Test entities --------------------

    @PersistedEntity("war_towns")
    static class TownWarData {
        @PersistedId("id")
        String id;

        @PersistedColumn("name")
        String name;

        @PersistedEmbedded(prefix = "shield_", nullable = true)
        ShieldDisplay shield;
    }

    @PersistedEntity("towns_non_nullable_shield")
    static class TownWithNonNullableShield {
        @PersistedId("id")
        String id;

        @PersistedColumn("name")
        String name;

        @PersistedEmbedded(prefix = "shield_") // nullable = false (default)
        ShieldDisplay shield;
    }

    static class AbstractItemLocation {
        @PersistedColumn("x")
        int x;

        @PersistedColumn("y")
        int y;

        @PersistedColumn("z")
        int z;
    }

    static class ShieldDisplay extends AbstractItemLocation {
        @PersistedColumn("level")
        int level;
    }

    @PersistedEntity("profiles_with_settings")
    static class ProfileWithSettings {
        @PersistedId("id")
        String id;

        @PersistedEmbedded(prefix = "settings_", nullable = true)
        SettingsWithDefaults settings;
    }

    static class SettingsWithDefaults {
        @PersistedColumn(value = "notifications", nullable = false, defaultValue = "true")
        boolean notifications = true;

        @PersistedColumn(value = "volume", nullable = false, defaultValue = "50")
        int volume = 50;
    }

    @PersistedEntity("entities_with_complex_settings")
    static class EntityWithComplexSettings {
        @PersistedId("id")
        String id;

        @PersistedEmbedded(prefix = "settings_", nullable = true)
        ComplexSettings settings;
    }

    // Simulates ExampleSettings behavior
    static class ComplexSettings {
        @PersistedColumn(value = "notify", nullable = false, defaultValue = "true")
        boolean notify = true;

        // No defaultValue annotation, but has Java default
        @PersistedColumn(value = "theme", length = 24)
        String theme = "default";
    }
}



