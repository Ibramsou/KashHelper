package fr.ibrakash.helper.jda.embed.spec;

import java.util.List;

public record ActionRowSpec(List<PersistentButtonSpec> buttons, SelectMenuSpec selectMenu) {
    public ActionRowSpec {
        buttons = buttons == null ? List.of() : List.copyOf(buttons);
    }
}

