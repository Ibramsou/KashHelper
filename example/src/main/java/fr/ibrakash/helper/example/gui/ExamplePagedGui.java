package fr.ibrakash.helper.example.gui;

import fr.ibrakash.helper.example.ExampleMenus;
import fr.ibrakash.helper.gui.invui.wrapper.PagedInvUiWrapper;
import fr.ibrakash.helper.utils.TextUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.stream.Stream;

public class ExamplePagedGui extends PagedInvUiWrapper<Material> {

    private final Player player;
    private boolean diamond = false;

    public ExamplePagedGui(Player player) {
        super(ExampleMenus.get().getPaged("paged-gui"));

        this.player = player;

        this.setAction("reload_items", (issuer, type, event, item) -> {
            this.diamond = !this.diamond;
            this.refresh();
        });
    }

    @Override
    public Component title() {
        return this.diamond ? TextUtil.replacedComponent("<green>Super Diamond: <white>%player%", this.getReplacers()) : super.title();
    }

    @Override
    public List<Material> getPagedObjects() {
        if (diamond) {
            return List.of(Material.DIAMOND, Material.APPLE);
        }
        return Stream.of(Material.values())
                .filter(material -> !material.isLegacy())
                .filter(Material::isBlock)
                .filter(material -> !material.isAir())
                .toList();
    }

    @Override
    public List<Object> pagedParsers(Material material) {
        return TextUtil.mergeReplacers(List.of("%material_name%", material.name()), this.getReplacers());
    }

    @Override
    public void clickPagedObject(Player issuer, Material object, ClickType type, InventoryClickEvent event) {
        issuer.sendMessage(TextUtil.replacedComponent("<green>Clicked on: <gray>%material%", "%material%", object.name()));
    }

    @Override
    public List<Object> getReplacers() {
        return List.of("%player%", this.player.getName());
    }
}
