package fr.ibrakash.helper.example.gui;

import fr.ibrakash.helper.configuration.objects.gui.PagedGuiConfig;
import fr.ibrakash.helper.example.ExampleGuiConfig;
import fr.ibrakash.helper.gui.invui.wrapper.PagedInvUiWrapper;
import fr.ibrakash.helper.utils.TextUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.stream.Stream;

public class ExamplePagedGui extends PagedInvUiWrapper<Material> {

    public ExamplePagedGui() {
        super(ExampleGuiConfig.get().getPagedGui());
    }

    @Override
    public List<Material> getPagedObjects() {
        return Stream.of(Material.values())
                .filter(material -> !material.isLegacy())
                .filter(Material::isBlock)
                .filter(material -> !material.isAir())
                .toList();
    }

    @Override
    public List<Object> pagedParsers(Material material) {
        return List.of("%material_name%", material.name());
    }

    @Override
    public void clickPagedObject(Player issuer, Material object, ClickType type, InventoryClickEvent event) {
        issuer.sendMessage(TextUtil.replacedComponent("<green>Clicked on: <gray>%material%", "%material%", object.name()));
    }

    @Override
    public List<Object> getReplacers() {
        return List.of();
    }
}
