package fr.ibrakash.helper.jda.embed.parser;

import fr.ibrakash.helper.jda.embed.spec.PersistentButtonStyle;
import fr.ibrakash.helper.jda.embed.spec.PersistentEmbedSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PersistentEmbedParserTest {

    private final PersistentEmbedParser parser = new PersistentEmbedParser();

    @Test
    void parsesGuildInfoTemplate() {
        PersistentEmbedSpec spec = this.parser.parse(List.of(
                "<container>",
                "  <text kind='title'>%guild_title% Infos</text>",
                "  <section>",
                "    <text>Locale language: %locale_display%</text>",
                "    <text>Economic value per member: %economic_value%€</text>",
                "  </section>",
                "  <actions>",
                "    <button style='primary' id='refresh_guild'>Refresh</button>",
                "    <button style='secondary' id='details_guild'>Details</button>",
                "  </actions>",
                "</container>"
        ));

        assertEquals("%guild_title% Infos", spec.title());
        assertEquals(2, spec.bodyLines().size());
        assertEquals(2, spec.buttons().size());
        assertEquals(PersistentButtonStyle.PRIMARY, spec.buttons().getFirst().style());
        assertEquals("refresh_guild", spec.buttons().getFirst().id());
    }

    @Test
    void rejectsInteractiveButtonWithoutId() {
        assertThrows(IllegalArgumentException.class, () -> this.parser.parse(List.of(
                "<container>",
                "  <actions>",
                "    <button style='primary'>No id</button>",
                "  </actions>",
                "</container>"
        )));
    }
}

