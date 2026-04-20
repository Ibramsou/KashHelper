package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import fr.ibrakash.helper.persistence.entity.PersistedDefaultId;
import fr.ibrakash.helper.persistence.entity.PersistedEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests that @PersistedDefaultId works correctly when placed on class declaration.
 */
class PersistedDefaultIdClassLevelTest {

    @PersistedEntity("test_entities")
    @PersistedDefaultId("entity_id")
    static class TestEntityWithClassLevelAnnotation {
        @PersistedColumn("entity_id")
        private long id;

        private String name;

        public TestEntityWithClassLevelAnnotation() {
        }
    }

    @Test
    void testPersistedDefaultIdOnClassLevel() {
        EntityModel<TestEntityWithClassLevelAnnotation, Long> model =
                EntityModel.from(TestEntityWithClassLevelAnnotation.class, Long.class);

        assertNotNull(model, "EntityModel should be created successfully");
        assertEquals("entity_id", model.idColumn().name(), "ID column name should be 'entity_id'");
        assertEquals("test_entities", model.namespace(), "Namespace should be 'test_entities'");
    }
}

