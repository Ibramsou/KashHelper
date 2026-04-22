package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import fr.ibrakash.helper.persistence.entity.PersistedDefaultId;
import fr.ibrakash.helper.persistence.entity.PersistedEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PersistedDefaultIdInheritedColumnTest {

    @Test
    void defaultIdFindsInheritedPersistedColumn() {
        EntityModel<InheritedMessageEntity, Long> model = EntityModel.from(InheritedMessageEntity.class, Long.class);

        assertNotNull(model);
        assertEquals("message_id", model.idColumn().name());
        assertEquals(long.class, EntityModel.inferIdType(InheritedMessageEntity.class));
    }

    static class BaseMessageEntity {

        @PersistedColumn("message_id")
        protected long messageId;
    }

    @PersistedEntity("inherited_messages")
    @PersistedDefaultId("message_id")
    static class InheritedMessageEntity extends BaseMessageEntity {

        @PersistedColumn("owner_id")
        private long ownerId;

        public InheritedMessageEntity() {
        }
    }
}

