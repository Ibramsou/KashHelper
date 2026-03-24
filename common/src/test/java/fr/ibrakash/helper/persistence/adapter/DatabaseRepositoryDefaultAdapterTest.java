package fr.ibrakash.helper.persistence.adapter;

import fr.ibrakash.helper.configuration.objects.database.ConfigJsonStorage;
import fr.ibrakash.helper.configuration.objects.database.ConfigPersistence;
import fr.ibrakash.helper.configuration.objects.database.ConfigSql;
import fr.ibrakash.helper.persistence.PersistenceType;
import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import fr.ibrakash.helper.persistence.entity.PersistedEntity;
import fr.ibrakash.helper.persistence.entity.PersistedId;
import fr.ibrakash.helper.platform.KashAddon;
import fr.ibrakash.helper.sql.SqlDriverType;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseRepositoryDefaultAdapterTest {

    @Test
    void jsonRepositoryWorksWithoutRegisteringOrActivatingAdapter() throws Exception {
        Path addonFolder = Files.createTempDirectory("kash-helper-json-repo");

        ConfigPersistence config = new ConfigPersistence()
                .type(PersistenceType.JSON)
                .json(new ConfigJsonStorage().folder("storage"));

        TestAddon addon = new TestAddon(addonFolder);
        try (DefaultRepository repository = new DefaultRepository(addon, config)) {
            repository.put(TestRecord.of("alpha", "Alpha", 42));
            repository.saveAll();
            assertNotNull(repository.getActiveAdapter());
        }

        try (DefaultRepository repository = new DefaultRepository(addon, config)) {
            TestRecord loaded = repository.deserializeData(TestRecord.class, "alpha");
            assertNotNull(loaded);
            assertEquals("Alpha", loaded.name);
            assertEquals(42, loaded.score);
        }
    }

    @Test
    void sqlRepositoryWorksWithoutRegisteringOrActivatingAdapter() throws Exception {
        Path addonFolder = Files.createTempDirectory("kash-helper-sql-repo");
        Path dbFile = addonFolder.resolve("test.db");

        ConfigPersistence config = new ConfigPersistence()
                .type(PersistenceType.SQL)
                .sql(new ConfigSql()
                        .driver(SqlDriverType.SQLITE)
                        .database(dbFile.toString())
                        .poolSize(2));

        TestAddon addon = new TestAddon(addonFolder);
        try (DefaultRepository repository = new DefaultRepository(addon, config)) {
            repository.put(TestRecord.of("beta", "Beta", 7));
            repository.saveAll();
            assertNotNull(repository.getActiveAdapter());
        }

        try (DefaultRepository repository = new DefaultRepository(addon, config)) {
            TestRecord loaded = repository.deserializeData(TestRecord.class, "beta");
            assertNotNull(loaded);
            assertEquals("Beta", loaded.name);
            assertEquals(7, loaded.score);
        }
    }

    @Test
    void registeredCustomAdapterStillTakesPrecedenceWithoutManualActivation() throws Exception {
        Path addonFolder = Files.createTempDirectory("kash-helper-custom-repo");
        Path dbFile = addonFolder.resolve("custom.db");

        ConfigPersistence config = new ConfigPersistence()
                .type(PersistenceType.SQL)
                .sql(new ConfigSql()
                        .driver(SqlDriverType.SQLITE)
                        .database(dbFile.toString())
                        .poolSize(2));

        try (CustomRepository repository = new CustomRepository(new TestAddon(addonFolder), config)) {
            repository.put(TestRecord.of("gamma", "Gamma", 99));
            repository.saveAll();
            assertTrue(repository.customAdapterActivated);
            assertNotNull(repository.getActiveAdapter());
        }
    }

    private static final class DefaultRepository extends DatabaseRepository {

        private final Map<String, TestRecord> cache = new LinkedHashMap<>();

        private DefaultRepository(KashAddon<?> addon, ConfigPersistence config) {
            super(addon, config);
        }

        private void put(TestRecord record) {
            this.cache.put(record.id, record);
        }

        @Override
        public void saveAll() {
            this.flushEntireData(this.cache, TestRecord.class);
        }
    }

    private static final class CustomRepository extends DatabaseRepository {

        private final Map<String, TestRecord> cache = new LinkedHashMap<>();
        private boolean customAdapterActivated;

        private CustomRepository(KashAddon<?> addon, ConfigPersistence config) {
            super(addon, config);
            this.registerAdapter(DatabaseAdapterType.SQL, TrackingSqlAdapter::new);
        }

        private void put(TestRecord record) {
            this.cache.put(record.id, record);
        }

        @Override
        public void saveAll() {
            this.flushEntireData(this.cache, TestRecord.class);
        }
    }

    private static final class TrackingSqlAdapter extends SqlAdapter<CustomRepository> {

        private TrackingSqlAdapter(CustomRepository repository) {
            super(repository);
            repository.customAdapterActivated = true;
        }
    }

    private static final class TestAddon extends KashAddon<Object> {

        private final File addonFolder;

        private TestAddon(Path addonFolder) {
            super(new Object());
            this.addonFolder = addonFolder.toFile();
        }

        @Override
        public File getAddonFolder() {
            return this.addonFolder;
        }
    }

    @PersistedEntity("test_records")
    public static final class TestRecord {

        @PersistedId("id")
        public String id;

        @PersistedColumn("name")
        public String name;

        @PersistedColumn("score")
        public int score;

        public TestRecord() {
        }

        static TestRecord of(String id, String name, int score) {
            TestRecord record = new TestRecord();
            record.id = id;
            record.name = name;
            record.score = score;
            return record;
        }
    }
}
