package fr.ibrakash.helper.jda.configuration.objects.display;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class JdaConfigMessageTest {

    @Test
    void serializesPlainTextMessage() {
        JdaConfigMessage message = new JdaConfigMessage("Hello %name%!");

        var serialized = message.serialized("%name%", "Kash");

        assertEquals("Hello Kash!", serialized.getContent());
        assertEquals(0, serialized.getEmbeds().size());
    }

    @Test
    void serializesEmbedTemplateMessage() {
        JdaConfigMessage message = new JdaConfigMessage(String.join("\n",
                "<container>",
                "  <text kind='title'>%guild_title% Infos</text>",
                "  <section>",
                "    <text>Average server score: %score%%</text>",
                "  </section>",
                "</container>"
        ));

        var serialized = message.serialized("%guild_title%", "My Guild", "%score%", 87);

        assertFalse(serialized.getEmbeds().isEmpty());
        assertEquals("My Guild Infos", serialized.getEmbeds().getFirst().getTitle());
        assertEquals("Average server score: 87%", serialized.getEmbeds().getFirst().getDescription());
    }
}

