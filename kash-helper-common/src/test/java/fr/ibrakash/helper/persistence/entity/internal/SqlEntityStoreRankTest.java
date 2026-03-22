package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.configuration.objects.database.ConfigSql;
import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import fr.ibrakash.helper.persistence.entity.PersistedEntity;
import fr.ibrakash.helper.persistence.entity.PersistedId;
import fr.ibrakash.helper.persistence.query.SortClause;
import fr.ibrakash.helper.persistence.sql.SqlPersistenceEngine;
import fr.ibrakash.helper.sql.SqlDriverType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SqlEntityStoreRankTest {

    @Test
    void bulkRanksAreResolvedWithExpectedOrder() throws Exception {
        Path db = Files.createTempFile("kash-helper-ranks", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<RankProfile, String> store = new SqlEntityStore<>(engine, RankProfile.class, String.class);
            store.saveAll(List.of(
                    RankProfile.of("a", 10, 50),
                    RankProfile.of("b", 5, 100),
                    RankProfile.of("c", 50, 70)
            ));

            List<SortClause> order = List.of(SortClause.desc("score"), SortClause.desc("points"));

            assertEquals(1, store.rankOf("b", order));
            assertEquals(2, store.rankOf("c", order));
            assertEquals(3, store.rankOf("a", order));

            Map<String, Integer> ranks = store.ranksOf(List.of("a", "b", "c"), order);
            assertEquals(3, ranks.get("a"));
            assertEquals(1, ranks.get("b"));
            assertEquals(2, ranks.get("c"));
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @PersistedEntity("rank_profiles")
    public static final class RankProfile {

        @PersistedId("id")
        private String id;

        @PersistedColumn("points")
        private int points;

        @PersistedColumn("score")
        private long score;

        public RankProfile() {
        }

        static RankProfile of(String id, int points, long score) {
            RankProfile profile = new RankProfile();
            profile.id = id;
            profile.points = points;
            profile.score = score;
            return profile;
        }
    }
}
