package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.configuration.objects.database.ConfigSql;
import fr.ibrakash.helper.persistence.entity.*;
import fr.ibrakash.helper.persistence.sql.SqlPersistenceEngine;
import fr.ibrakash.helper.sql.SqlDriverType;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests that @PersistedRelation correctly handles object elements with inherited fields.
 */
class RelationInheritanceTest {

    @Test
    void relation_objectWithInheritedFields_roundTrip() throws Exception {
        Path db = Files.createTempFile("kash-relation-inherit", ".db");

        ConfigSql config = new ConfigSql()
                .driver(SqlDriverType.SQLITE)
                .database(db.toString())
                .poolSize(2);

        try (SqlPersistenceEngine engine = new SqlPersistenceEngine(config)) {
            SqlEntityStore<ParentEntity, String> store = new SqlEntityStore<>(engine, ParentEntity.class, String.class);

            ParentEntity entity = new ParentEntity();
            entity.id = "parent-1";
            entity.items = new ArrayList<>();

            ChildItem item1 = new ChildItem();
            item1.baseField = "base-value";
            item1.baseNumber = 10;
            item1.childField = "child-value";
            entity.items.add(item1);

            ChildItem item2 = new ChildItem();
            item2.baseField = "base-2";
            item2.baseNumber = 20;
            item2.childField = "child-2";
            entity.items.add(item2);

            store.save(entity);

            ParentEntity loaded = store.find("parent-1").orElseThrow();
            assertNotNull(loaded.items);
            assertEquals(2, loaded.items.size());

            ChildItem loadedItem1 = loaded.items.get(0);
            assertEquals("base-value", loadedItem1.baseField);
            assertEquals(10, loadedItem1.baseNumber);
            assertEquals("child-value", loadedItem1.childField);

            ChildItem loadedItem2 = loaded.items.get(1);
            assertEquals("base-2", loadedItem2.baseField);
            assertEquals(20, loadedItem2.baseNumber);
            assertEquals("child-2", loadedItem2.childField);
        } finally {
            Files.deleteIfExists(db);
        }
    }

    // ---------- Test entities ----------

    @PersistedEntity("parent_entities")
    static class ParentEntity {
        @PersistedId("id") String id;

        @PersistedRelation(table = "parent_items", joinColumn = "parent_id", prefix = "item_")
        List<ChildItem> items;
    }

    static class BaseItem {
        @PersistedColumn(value = "base_field", length = 128)
        String baseField;

        @PersistedColumn("base_number")
        int baseNumber;
    }

    static class ChildItem extends BaseItem {
        @PersistedColumn(value = "child_field", length = 128)
        String childField;

        public ChildItem() {}
    }
}

