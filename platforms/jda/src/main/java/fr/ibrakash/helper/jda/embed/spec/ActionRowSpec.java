package fr.ibrakash.helper.jda.embed.spec;

import java.util.List;

/**
 * Spec for an {@code <actions>} row — holds buttons and/or one select menu.
 */
public record ActionRowSpec(List<PersistentButtonSpec> buttons, SelectMenuSpec selectMenu) {
    public ActionRowSpec {
        buttons = buttons == null ? List.of() : List.copyOf(buttons);
    }
}

