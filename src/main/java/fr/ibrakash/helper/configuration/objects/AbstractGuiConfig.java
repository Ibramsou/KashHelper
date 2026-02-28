package fr.ibrakash.helper.configuration.objects;

import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.stream.ConfigStream;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;

@ConfigSerializable
public abstract class AbstractGuiConfig extends ConfigStream  {

    protected String title = "<gray>Example Title";
    protected List<String> shape = List.of(
            "# # # # # # # # #",
            "# X X X X X X X #",
            "# X X X X X X X #",
            "# # # # # # # # #"
    );
    protected List<ConfigGuiItem> items = List.of(
            new ConfigGuiItem()
                    .ingredientCharacter('#')
                    .material(Material.GRAY_STAINED_GLASS_PANE)
                    .displayName(" "),
            new ConfigGuiItem()
                    .ingredientCharacter('X')
                    .material(Material.DIAMOND)
                    .displayName("<green>Example Item")
                    .lore(List.of("<gray>Example Lore."))
                    .actions("example_action")
    );

    public String getTitle() {
        return title;
    }

    public List<String> getShape() {
        return shape;
    }

    public List<ConfigGuiItem> getItems() {
        return items;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setShape(List<String> shape) {
        this.shape = shape;
    }

    public void setItems(List<ConfigGuiItem> items) {
        this.items = items;
    }
}
