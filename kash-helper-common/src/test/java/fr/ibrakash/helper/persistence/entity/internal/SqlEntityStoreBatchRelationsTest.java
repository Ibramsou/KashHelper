package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.configuration.objects.database.ConfigSql;
import fr.ibrakash.helper.persistence.sql.SqlPersistenceEngine;
import fr.ibrakash.helper.persistence.entity.PersistedEntity;
import fr.ibrakash.helper.persistence.entity.PersistedId;
import fr.ibrakash.helper.persistence.entity.PersistedRelation;
import fr.ibrakash.helper.sql.SqlDriverType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlEntityStoreBatchRelationsTest {

    @Test
    void saveAllBatchesRelationsAndKeepsLastEntityStatePerId() throws Exception {
        Path db = Files.createTempFile("kash-helper-relations", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<TestProfile, String> store = new SqlEntityStore<>(engine, TestProfile.class, String.class);

            TestProfile alpha = TestProfile.of("alpha", List.of("a", "b"));
            TestProfile beta = TestProfile.of("beta", List.of("x"));
            store.saveAll(List.of(alpha, beta));

            TestProfile alphaUpdated = TestProfile.of("alpha", List.of("c", "d"));
            TestProfile betaCleared = TestProfile.of("beta", List.of());
            TestProfile alphaOldState = TestProfile.of("alpha", List.of("stale"));

            // Last entity for the same id must win.
            store.saveAll(List.of(alphaOldState, betaCleared, alphaUpdated));

            List<TestProfile> loaded = store.findAllByIds(List.of("alpha", "beta"));
            assertEquals(2, loaded.size());
            assertEquals(List.of("c", "d"), loaded.get(0).tags);
            assertEquals(List.of(), loaded.get(1).tags);

            int relationRows = engine.getSqlDatabase().resultStatement(stmt -> {
                var rs = stmt.executeQuery("SELECT COUNT(*) FROM test_profile_tags");
                rs.next();
                return rs.getInt(1);
            });
            assertEquals(2, relationRows);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @PersistedEntity("test_profiles")
    public static final class TestProfile {

        @PersistedId("id")
        private String id;

        @PersistedRelation(table = "test_profile_tags", joinColumn = "profile_id", valueColumn = "tag_value")
        private List<String> tags = new ArrayList<>();

        public TestProfile() {
        }

        static TestProfile of(String id, List<String> tags) {
            TestProfile profile = new TestProfile();
            profile.id = id;
            profile.tags = new ArrayList<>(tags);
            return profile;
        }
    }
}

