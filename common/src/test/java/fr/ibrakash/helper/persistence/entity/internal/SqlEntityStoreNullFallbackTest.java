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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SqlEntityStoreNullFallbackTest {

    @Test
    void saveUsesFallbacksWhenValuesAreNull() throws Exception {
        Path db = Files.createTempFile("kash-helper-null-fallback", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<NullFallbackProfile, String> store = new SqlEntityStore<>(engine, NullFallbackProfile.class, String.class);

            NullFallbackProfile profile = new NullFallbackProfile();
            profile.id = "profile-1";
            profile.displayName = null;
            profile.optionalNote = null;
            profile.settings = null;

            store.save(profile);

            NullFallbackProfile loaded = store.find("profile-1").orElseThrow();
            assertEquals("anonymous", loaded.displayName);
            assertNull(loaded.optionalNote);

            assertNotNull(loaded.settings);
            assertEquals(true, loaded.settings.notify);
            assertEquals(0, loaded.settings.level);
            assertNull(loaded.settings.theme);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @PersistedEntity("null_fallback_profiles")
    static class NullFallbackProfile {

        @PersistedId("id")
        private String id;

        @PersistedColumn(value = "display_name", nullable = false, defaultValue = "'anonymous'")
        private String displayName;

        @PersistedColumn(value = "optional_note")
        private String optionalNote;

        @PersistedEmbedded(prefix = "settings_")
        private NullFallbackSettings settings;
    }

    static class NullFallbackSettings {

        @PersistedColumn(value = "notify", nullable = false, defaultValue = "true")
        private Boolean notify;

        @PersistedColumn(value = "level")
        private int level;

        @PersistedColumn(value = "theme")
        private String theme;
    }
}

