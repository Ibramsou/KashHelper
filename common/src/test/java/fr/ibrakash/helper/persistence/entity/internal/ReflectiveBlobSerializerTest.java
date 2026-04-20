package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.configuration.objects.database.ConfigSql;
import fr.ibrakash.helper.persistence.entity.*;
import fr.ibrakash.helper.persistence.sql.SqlPersistenceEngine;
import fr.ibrakash.helper.sql.SqlDriverType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for reflective blob serialization (auto-serializer for POJOs).
 */
class ReflectiveBlobSerializerTest {

    @Test
    void reflectiveBlob_roundTrip() throws Exception {
        Path db = Files.createTempFile("kash-reflective-blob", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<BlobEntity, String> store = new SqlEntityStore<>(engine, BlobEntity.class, String.class);

            BlobEntity entity = new BlobEntity();
            entity.id = "blob-1";
            entity.data = new BlobData();
            entity.data.message = "hello world";
            entity.data.count = 42;
            entity.data.active = true;
            entity.data.score = 3.14;

            store.save(entity);

            BlobEntity loaded = store.find("blob-1").orElseThrow();
            assertNotNull(loaded.data);
            assertEquals("hello world", loaded.data.message);
            assertEquals(42, loaded.data.count);
            assertTrue(loaded.data.active);
            assertEquals(3.14, loaded.data.score, 0.001);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void reflectiveBlob_nullData_roundTrip() throws Exception {
        Path db = Files.createTempFile("kash-reflective-blob-null", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<BlobEntity, String> store = new SqlEntityStore<>(engine, BlobEntity.class, String.class);

            BlobEntity entity = new BlobEntity();
            entity.id = "blob-null";
            entity.data = null;

            store.save(entity);

            BlobEntity loaded = store.find("blob-null").orElseThrow();
            // Null blob should deserialize to default instance
            assertNotNull(loaded.data);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void reflectiveBlob_withEnum() throws Exception {
        Path db = Files.createTempFile("kash-reflective-blob-enum", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<BlobWithEnumEntity, String> store = new SqlEntityStore<>(engine, BlobWithEnumEntity.class, String.class);

            BlobWithEnumEntity entity = new BlobWithEnumEntity();
            entity.id = "blob-enum-1";
            entity.data = new BlobWithEnum();
            entity.data.status = Status.ACTIVE;
            entity.data.label = "test";

            store.save(entity);

            BlobWithEnumEntity loaded = store.find("blob-enum-1").orElseThrow();
            assertNotNull(loaded.data);
            assertEquals(Status.ACTIVE, loaded.data.status);
            assertEquals("test", loaded.data.label);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    @Test
    void reflectiveBlob_withInheritance() throws Exception {
        Path db = Files.createTempFile("kash-reflective-blob-inherit", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<InheritedBlobEntity, String> store = new SqlEntityStore<>(engine, InheritedBlobEntity.class, String.class);

            InheritedBlobEntity entity = new InheritedBlobEntity();
            entity.id = "blob-inherit-1";
            entity.data = new ExtendedBlobData();
            entity.data.message = "parent field";
            entity.data.count = 7;
            entity.data.active = true;
            entity.data.score = 1.5;
            ((ExtendedBlobData) entity.data).extra = "child field";

            store.save(entity);

            InheritedBlobEntity loaded = store.find("blob-inherit-1").orElseThrow();
            assertNotNull(loaded.data);
            assertTrue(loaded.data instanceof ExtendedBlobData);
            assertEquals("parent field", loaded.data.message);
            assertEquals(7, loaded.data.count);
            assertEquals("child field", ((ExtendedBlobData) loaded.data).extra);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    // ---------- Test entities ----------

    @PersistedEntity("blob_entities")
    static class BlobEntity {
        @PersistedId("id") String id;
        @PersistedBlob("data") BlobData data;
    }

    static class BlobData {
        String message;
        int count;
        boolean active;
        double score;

        public BlobData() {}
    }

    @PersistedEntity("blob_enum_entities")
    static class BlobWithEnumEntity {
        @PersistedId("id") String id;
        @PersistedBlob("data") BlobWithEnum data;
    }

    static class BlobWithEnum {
        Status status;
        String label;

        public BlobWithEnum() {}
    }

    enum Status { INACTIVE, ACTIVE, BANNED }

    @PersistedEntity("inherited_blob_entities")
    static class InheritedBlobEntity {
        @PersistedId("id") String id;
        @PersistedBlob("data") ExtendedBlobData data;
    }

    static class ExtendedBlobData extends BlobData {
        String extra;

        public ExtendedBlobData() {}
    }
}

