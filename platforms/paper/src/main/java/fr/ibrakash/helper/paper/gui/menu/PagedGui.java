package fr.ibrakash.helper.paper.gui.menu;

import fr.ibrakash.helper.paper.configuration.objects.gui.ConfigPagedGui;
import fr.ibrakash.helper.paper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.paper.gui.GuiPageHandler;
import fr.ibrakash.helper.paper.gui.GuiPagedActionConsumer;
import fr.ibrakash.helper.paper.gui.GuiPagedObject;
import fr.ibrakash.helper.paper.gui.inventory.item.GuiMenuItem;
import fr.ibrakash.helper.paper.gui.inventory.layout.GuiLayout;
import fr.ibrakash.helper.paper.item.replacer.ListedItemReplacer;
import fr.ibrakash.helper.paper.text.PaperTextReplacer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class PagedGui<O> extends AbstractInventoryGui<ConfigPagedGui> implements GuiPageHandler<O> {

    private final Map<String, GuiPagedActionConsumer<O>> pagedActionMap = new LinkedHashMap<>();
    private List<Integer> contentSlots = List.of();
    private int page = 0;

    public PagedGui(ConfigPagedGui config) {
        super(config);
        this.setPageActions(this);
    }

    @Override
    protected boolean shouldUseAsStaticIngredient(ConfigGuiItem item) {
        return this.config.getPagedItem()
                .map(pagedItem -> !Objects.equals(pagedItem.getId(), item.getId()))
                .orElse(true);
    }

    @Override
    protected void afterBuildLayout(GuiLayout layout) {
        this.contentSlots = this.config.getPagedItem()
                .map(configItem -> List.copyOf(layout.slots(configItem.getShapeCharacter())))
                .orElse(List.of());
    }

    @Override
    public void refresh() {
        this.page = 0;
        super.refresh();
    }

    @Override
    protected Map<Integer, GuiMenuItem> buildSlotItems() {
        Map<Integer, GuiMenuItem> slotItems = super.buildSlotItems();
        this.config.getPagedItem().ifPresent(pagedItem -> {
            List<GuiPagedObject<O>> objects = this.loadPageObjects();
            if (objects.isEmpty() || this.contentSlots.isEmpty()) {
                return;
            }

            int startIndex = this.page * this.contentSlots.size();
            ListedItemReplacer<O> pagedItemReplacer = this.pagedItemReplacer();

            for (int i = 0; i < this.contentSlots.size(); i++) {
                int objectIndex = startIndex + i;
                int slot = this.contentSlots.get(i);

                if (objectIndex >= objects.size()) {
                    slotItems.remove(slot);
                    continue;
                }

                GuiPagedObject<O> pagedObject = objects.get(objectIndex);
                PaperTextReplacer replacer = PaperTextReplacer.create()
                        .merge(this.pagedTextReplacer(pagedObject.object()))
                        .add("%page_item_index%", objectIndex)
                        .add("%page_item_number%", objectIndex + 1);

                GuiMenuItem item = new GuiMenuItem(this, pagedItem, configItem -> pagedItemReplacer == null
                        ? configItem.build(replacer, this.itemReplacer)
                        : configItem.build(replacer, pagedItemReplacer, pagedObject.object()));
                item.setDefaultConsumer(pagedObject.consumer());
                slotItems.put(slot, item);
            }
        });

        return slotItems;
    }

    @Override
    public int maxPages() {
        if (this.contentSlots.isEmpty()) {
            return 1;
        }
        int size = this.loadPageObjects().size();
        return Math.max(1, (int) Math.ceil(size / (double) this.contentSlots.size()));
    }

    @Override
    public int currentPage() {
        return Math.min(this.page + 1, this.maxPages());
    }

    @Override
    public void nextPage() {
        int maxPages = this.maxPages();
        if (this.page + 1 >= maxPages) {
            return;
        }
        this.page++;
        super.refresh();
    }

    @Override
    public void previousPage() {
        if (this.page == 0) {
            return;
        }
        this.page--;
        super.refresh();
    }

    @Override
    public Map<String, GuiPagedActionConsumer<O>> getPagedActionMap() {
        return this.pagedActionMap;
    }
}
