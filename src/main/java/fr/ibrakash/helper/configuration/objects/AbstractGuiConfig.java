package fr.ibrakash.helper.configuration.objects;

import fr.ibrakash.helper.configuration.objects.action.ConfigAction;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.configuration.objects.item.ConfigItems;
import fr.ibrakash.helper.configuration.objects.stream.ConfigStream;
import org.bukkit.Material;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ConfigSerializable
public abstract class AbstractGuiConfig extends ConfigStream  {

    protected String title = "<gray>Example Title";
    protected List<String> shape = List.of(
            "# # # # # # # # #",
            "# X X X X X X X #",
            "# X X X X X X X #",
            "# # # # # # # # #"
    );
    protected Map<String, ConfigGuiItem> items = Map.of(
            "decoration-item", ConfigItems.GLASS_PANE_DECORATION,
            "example-item", new ConfigGuiItem()
                    .ingredientCharacter('X')
                    .material(Material.DIAMOND)
                    .displayName("<green>Example Item")
                    .lore(List.of("<gray>Example Lore."))
                    .actions(new ConfigAction().execute(List.of("example_action")))
    );

    private transient boolean loaded = false;

    public String getTitle() {
        return title;
    }

    public List<String> getShape() {
        return shape;
    }

    public Map<String, ConfigGuiItem> getItems() {
        if (loaded) return items;
        items.forEach((s, item) -> item.setId(s));
        this.loaded = true;
        return items;
    }

    public Optional<ConfigGuiItem> getItem(String key) {
        return Optional.ofNullable(this.getItems().get(key));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setShape(List<String> shape) {
        this.shape = shape;
    }

    public void setItems(Map<String, ConfigGuiItem> items) {
        this.items = items;
    }
}
