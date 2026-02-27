package fr.ibrakash.helper.gui;

import fr.ibrakash.helper.utils.TextUtil;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;

public interface GuiPageHandler<O> {

    default void listPageObjects(PagedItemConsumer consumer) {
        List<GuiPagedObject<O>> objects = this.loadPageObjects();
        for (int i = 0; i < objects.size(); i++) {
            GuiPagedObject<O> object = objects.get(i);
            List<Object> replacers = TextUtil.mergeReplacers(this.pagedParsers(object.object()),
                    "%page_item_index%", i,
                    "%page_item_number%", i + 1
            );

            consumer.accept(replacers, object.consumer());
        }
    }

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

    @FunctionalInterface
    interface PagedItemConsumer {
        void accept(List<Object> replacers, GuiActionConsumer clickConsumer);
    }
}
