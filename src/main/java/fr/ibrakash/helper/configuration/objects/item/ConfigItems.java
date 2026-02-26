package fr.ibrakash.helper.configuration.objects.item;

import org.bukkit.Material;

public interface ConfigItems {

    ConfigGuiItem GLASS_PANE_DECORATION = new ConfigGuiItem()
                            .ingredientCharacter('#')
                            .material(Material.GRAY_STAINED_GLASS_PANE)
                            .displayName(" ");

    ConfigGuiItem PREVIOUS_PAGE = new ConfigGuiItem()
            .ingredientCharacter('P')
            .material(Material.PAPER)
            .displayName("<gray>Previous Page")
            .actions("previous_page");

    ConfigGuiItem NEXT_PAGE = new ConfigGuiItem()
            .ingredientCharacter('N')
            .material(Material.PAPER)
            .displayName("<gray>Next Page")
            .actions("next_page");
}
