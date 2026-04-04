package fr.ibrakash.helper.persistence.entity.internal;

import fr.ibrakash.helper.persistence.entity.PersistedColumn;
import fr.ibrakash.helper.persistence.entity.PersistedEmbedded;
import fr.ibrakash.helper.persistence.entity.PersistedEntity;
import fr.ibrakash.helper.persistence.entity.PersistedId;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class EntityModelEmbeddedInheritanceTest {

    @Test
    void embeddedColumnsIncludeInheritedFieldsAndCanBeWritten() {
        EntityModel<TownShieldProfile, String> model = EntityModel.from(TownShieldProfile.class, String.class);

        Set<String> columns = model.columns().stream()
                .map(EntityModel.Column::name)
                .collect(Collectors.toSet());

        assertEquals(Set.of("shield_level", "shield_x", "shield_y", "shield_z"), columns);

        TownShieldProfile profile = new TownShieldProfile();
        EntityModel.Column x = model.resolveColumn("shield_x");
        EntityModel.Column level = model.resolveColumn("shield_level");

        model.writeColumnValue(profile, x, 128);
        model.writeColumnValue(profile, level, 3);

        assertNotNull(profile.shield);
        assertEquals(128, model.readColumnValue(profile, x));
        assertEquals(3, model.readColumnValue(profile, level));
    }

    @PersistedEntity("town_shield_profiles")
    static class TownShieldProfile {

        @PersistedId("id")
        private String id = "town:spawn";

        @PersistedEmbedded(prefix = "shield_")
        private ShieldDisplayData shield;
    }

    static class DisplayLocationData {

        @PersistedColumn("x")
        private int x;

        @PersistedColumn("y")
        private int y;

        @PersistedColumn("z")
        private int z;
    }

    static class ShieldDisplayData extends DisplayLocationData {

        @PersistedColumn("level")
        private int level;
    }
}
