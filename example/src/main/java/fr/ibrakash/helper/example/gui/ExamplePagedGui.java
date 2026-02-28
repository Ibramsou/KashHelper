package fr.ibrakash.helper.example.gui;

import fr.ibrakash.helper.example.ExampleMenus;
import fr.ibrakash.helper.gui.invui.wrapper.PagedInvUiWrapper;
import fr.ibrakash.helper.stream.StreamFilter;
import fr.ibrakash.helper.text.TextReplacer;
import fr.ibrakash.helper.text.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ExamplePagedGui extends PagedInvUiWrapper<Material> {

    private final StreamFilter<Material> filter;
    private boolean diamond = false;

    public ExamplePagedGui(Player player) {
        super(ExampleMenus.get().getPaged("paged-gui"));

        this.filter = StreamFilter.<Material>of(this)
                .add("wood_only", stream -> stream.filter(material -> material.name().contains("WOOD")))
                .add("ores_only", stream -> stream
                        .filter(material -> material.name().contains("ORE"))
                )
                .add("sort_diamonds", stream ->
                        stream.sort(Comparator.comparingInt(material -> material.name().contains("DIAMOND") ? 0 : 1)))
                .add("sort_names", stream -> stream.sort(Comparator.comparing(Material::name)));

        this.setAction("reload_items", (issuer, type, event, item) -> {
            this.diamond = !this.diamond;
            this.refresh();
        });

        this.replacer.add("%player%", player.getName());
    }

    @Override
    public Component title() {
        return this.diamond ? this.replacer.deserialize("<green>Super Diamond: <white>%player%") : super.title();
    }

    @Override
    public List<Material> getPagedObjects() {
        if (diamond) {
            return List.of(Material.DIAMOND, Material.APPLE);
        }
        return this.filter.applyFilters(Stream.of(Material.values())
                .filter(material -> !material.isLegacy())
                .filter(Material::isBlock)
                .filter(material -> !material.isAir()));
    }

    @Override
    public TextReplacer pagedReplacer(Material material) {
        return TextReplacer.of(this.replacer).add("%material_name%", material.name());
    }

    @Override
    public void clickPagedObject(Player issuer, Material object, ClickType type, InventoryClickEvent event) {
        issuer.sendMessage(TextUtil.replacedComponent("<green>Clicked on: <gray>%material%", "%material%", object.name()));
    }
}
