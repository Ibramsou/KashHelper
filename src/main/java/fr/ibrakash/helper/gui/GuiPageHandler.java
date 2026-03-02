package fr.ibrakash.helper.gui;

import fr.ibrakash.helper.item.replacer.ListedItemReplacer;
import fr.ibrakash.helper.text.TextReplacer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GuiPageHandler<O> {

    default void listPageObjects(PagedItemConsumer<O> consumer) {
        List<GuiPagedObject<O>> objects = this.loadPageObjects();

        for (int i = 0; i < objects.size(); i++) {
            GuiPagedObject<O> object = objects.get(i);
            TextReplacer replacer = TextReplacer.create()
                    .merge(this.pagedTextReplacer(object.object()))
                    .add("%page_item_index%", i)
                    .add("%page_item_number%", i + 1);

            consumer.accept(replacer, object);
        }
    }

    default void setPageActions(GuiWrapper<?, ?, ?> wrapper) {
        wrapper.action("next_page", (issuer, type, event, item) ->
                this.nextPage());
        wrapper.action("previous_page", (issuer, type, event, item) ->
                this.previousPage());
    }

    default List<GuiPagedObject<O>> loadPageObjects() {
        return this.getPagedObjects().stream().map(o ->
                new GuiPagedObject<>(o, (issuer, type, event, item) ->
                {
                    this.clickPagedObject(issuer, o, event.getClick(), event);
                    this.doPagedActions(o, issuer, event, item);
                })
        ).toList();
    }

    default Optional<GuiPagedActionConsumer<O>> getPagedAction(String key) {
        return Optional.ofNullable(this.getPagedActionMap().get(key));
    }


    default void doPagedActions(O object, Player player, InventoryClickEvent event, GuiItemWrapper wrapper) {
        wrapper.getConfigItem().getActions().runActions(event.getClick(), execute ->
                this.getPagedAction(execute).ifPresent(consumer ->
                        consumer.doAction(object, player, event.getClick(), event, wrapper)));
    }

    default void pagedAction(String action, GuiPagedActionConsumer<O> consumer) {
        this.getPagedActionMap().put(action, consumer);
    }

    Map<String, GuiPagedActionConsumer<O>> getPagedActionMap();

    List<O> getPagedObjects();

    TextReplacer pagedTextReplacer(O object);

    ListedItemReplacer<O> pagedItemReplacer();

    default void clickPagedObject(Player issuer, O object, ClickType type, InventoryClickEvent event) {}

    int maxPages();

    int currentPage();

    void nextPage();

    void previousPage();

    @FunctionalInterface
    interface PagedItemConsumer<O> {
        void accept(TextReplacer replacer, GuiPagedObject<O> pagedObject);
    }
}
