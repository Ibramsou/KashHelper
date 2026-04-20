package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.configuration.objects.database.ConfigSql;
import fr.ibrakash.helper.persistence.entity.*;
import fr.ibrakash.helper.persistence.sql.SqlPersistenceEngine;
import fr.ibrakash.helper.sql.SqlDriverType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link PersistenceLifecycle} integration in SqlEntityStore.
 */
class PersistenceLifecycleTest {

    @Test
    void entity_onDeserialized_calledAfterLoad() throws Exception {
        Path db = Files.createTempFile("kash-lifecycle-entity", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<LifecycleEntity, String> store = new SqlEntityStore<>(engine, LifecycleEntity.class, String.class);

            LifecycleEntity entity = new LifecycleEntity();
            entity.id = "lc-1";
            entity.name = "test";
            store.save(entity);

            LifecycleEntity loaded = store.find("lc-1").orElseThrow();
            assertTrue(loaded.deserializedCalled, "onDeserialized() should be called after find()");
            assertEquals("test", loaded.name);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void entity_onDeserialized_calledAfterFindAll() throws Exception {
        Path db = Files.createTempFile("kash-lifecycle-findall", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<LifecycleEntity, String> store = new SqlEntityStore<>(engine, LifecycleEntity.class, String.class);

            LifecycleEntity e1 = new LifecycleEntity();
            e1.id = "lc-a";
            e1.name = "aaa";
            store.save(e1);

            LifecycleEntity e2 = new LifecycleEntity();
            e2.id = "lc-b";
            e2.name = "bbb";
            store.save(e2);

            var all = store.findAll();
            assertEquals(2, all.size());
            for (var item : all) {
                assertTrue(item.deserializedCalled, "onDeserialized() should be called for each entity in findAll()");
            }
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void embedded_onDeserialized_calledBeforeParent() throws Exception {
        Path db = Files.createTempFile("kash-lifecycle-embedded", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<EntityWithLifecycleEmbed, String> store = new SqlEntityStore<>(engine, EntityWithLifecycleEmbed.class, String.class);

            EntityWithLifecycleEmbed entity = new EntityWithLifecycleEmbed();
            entity.id = "embed-lc-1";
            entity.data = new LifecycleEmbedded();
            entity.data.value = 42;
            store.save(entity);

            EntityWithLifecycleEmbed loaded = store.find("embed-lc-1").orElseThrow();
            assertTrue(loaded.deserializedCalled, "Parent onDeserialized() should be called");
            assertNotNull(loaded.data);
            assertTrue(loaded.data.deserializedCalled, "Embedded onDeserialized() should be called");
            assertEquals(42, loaded.data.value);
            // Embedded should be called before parent
            assertTrue(loaded.data.deserializedOrder < loaded.deserializedOrder,
                    "Embedded lifecycle should fire before parent");
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void nonLifecycleEntity_noError() throws Exception {
        Path db = Files.createTempFile("kash-lifecycle-noop", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<SimpleEntity, String> store = new SqlEntityStore<>(engine, SimpleEntity.class, String.class);

            SimpleEntity entity = new SimpleEntity();
            entity.id = "simple-1";
            entity.name = "hello";
            store.save(entity);

            SimpleEntity loaded = store.find("simple-1").orElseThrow();
            assertEquals("hello", loaded.name);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    // ---------- Shared counter for ordering ----------
    private static int globalCounter = 0;

    // ---------- Test entities ----------

    @PersistedEntity("lifecycle_entities")
    static class LifecycleEntity implements PersistenceLifecycle {
        @PersistedId("id") String id;
        @PersistedColumn("name") String name;
        transient boolean deserializedCalled;
        transient int deserializedOrder;

        @Override
        public void onDeserialized() {
            this.deserializedCalled = true;
            this.deserializedOrder = globalCounter++;
        }
    }

    @PersistedEntity("embed_lifecycle_entities")
    static class EntityWithLifecycleEmbed implements PersistenceLifecycle {
        @PersistedId("id") String id;
        @PersistedEmbedded(prefix = "data_")
        LifecycleEmbedded data;
        transient boolean deserializedCalled;
        transient int deserializedOrder;

        @Override
        public void onDeserialized() {
            this.deserializedCalled = true;
            this.deserializedOrder = globalCounter++;
        }
    }

    static class LifecycleEmbedded implements PersistenceLifecycle {
        @PersistedColumn("value") int value;
        transient boolean deserializedCalled;
        transient int deserializedOrder;

        @Override
        public void onDeserialized() {
            this.deserializedCalled = true;
            this.deserializedOrder = globalCounter++;
        }
    }

    @PersistedEntity("simple_entities")
    static class SimpleEntity {
        @PersistedId("id") String id;
        @PersistedColumn("name") String name;
    }
}

