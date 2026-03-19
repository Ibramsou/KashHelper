package fr.ibrakash.helper.example.gui;

import fr.ibrakash.helper.example.ExamplePlugin;
import fr.ibrakash.helper.paper.gui.invui.wrapper.PagedInvUiWrapper;
import fr.ibrakash.helper.paper.item.ItemUtil;
import fr.ibrakash.helper.paper.item.replacer.ListedItemReplacer;
import fr.ibrakash.helper.stream.StreamFilter;
import fr.ibrakash.helper.paper.text.PaperTextReplacer;
import fr.ibrakash.helper.paper.text.PaperTextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class ExamplePagedGui extends PagedInvUiWrapper<Material> {

    private final StreamFilter<Material> filter;
    private boolean diamond = false;

    public ExamplePagedGui(ExamplePlugin plugin, Player player) {
        super(plugin.getExampleMenus().getPaged("paged-gui"));

        this.filter = StreamFilter.<Material>of(this)
                .add("wood_only", stream -> stream.filter(material -> material.name().contains("WOOD")))
                .add("ores_only", stream -> stream
                        .filter(material -> material.name().contains("ORE"))
                )
                .add("sort_diamonds", stream ->
                        stream.sort(Comparator.comparingInt(material -> material.name().contains("DIAMOND") ? 0 : 1)))
                .add("sort_names", stream -> stream.sort(Comparator.comparing(Material::name)));

        this.action("reload_items", (issuer, type, event, item) -> {
            this.diamond = !this.diamond;
            this.refresh();
        });

        this.pagedAction("close_and_message", (object, issuer, type, event, item) -> {
            issuer.closeInventory();
            issuer.sendMessage(PaperTextUtil.get().replacedComponent("<green>Closed on " + ItemUtil.extractName(new ItemStack(object))));
        });

        this.pagedAction("message", (object, issuer, type, event, item) -> {
            issuer.sendMessage(PaperTextUtil.get().replacedComponent("<green>Closed on " + object.name()));
        });

        this.textReplacer.add("%player%", player.getName());
    }

    @Override
    public Component title() {
        return this.diamond ? this.textReplacer.deserialize("<green>Super Diamond: <white>%player%") : super.title();
    }

    @Override
    public void handleClose(boolean byPlayer) {}

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
    public PaperTextReplacer pagedTextReplacer(Material material) {
        return PaperTextReplacer.of(this.textReplacer)
                .add("%material_name%", material.name());
    }

    @Override
    public ListedItemReplacer<Material> pagedItemReplacer() {
        return ListedItemReplacer.<Material>create()
                .material("%material_name%", material -> material);
    }
}
