package fr.ibrakash.helper.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public interface GuiPageHandler<O> {

    default void setPageActions(GuiWrapper<?, ?, ?> wrapper) {
        wrapper.setAction("next_page", (issuer, type, event, item) ->
                this.nextPage());
        wrapper.setAction("previous_page", (issuer, type, event, item) ->
                this.previousPage());
    }

    default List<GuiPagedObject<O>> loadPageObjects() {
        return this.getPagedObjects().stream().map(o ->
                new GuiPagedObject<>(o, (issuer, type, event, item) -> this.clickPagedObject(issuer, o, type, event)))
                .toList();
    }

    List<O> getPagedObjects();

    List<Object> pagedParsers(O object);

    void clickPagedObject(Player issuer, O object, ClickType type, InventoryClickEvent event);

    int maxPages();

    int currentPage();

    void nextPage();

    void previousPage();
}
