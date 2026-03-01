package fr.ibrakash.helper.gui.invui.wrapper;

import fr.ibrakash.helper.configuration.objects.gui.ConfigPagedGui;
import fr.ibrakash.helper.configuration.objects.item.ConfigGuiItem;
import fr.ibrakash.helper.gui.GuiPageHandler;
import fr.ibrakash.helper.gui.GuiPagedActionConsumer;
import fr.ibrakash.helper.gui.GuiPagedObject;
import fr.ibrakash.helper.gui.invui.InvUiItem;
import fr.ibrakash.helper.gui.invui.InvUiWrapper;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;

import java.util.*;

public abstract class PagedInvUiWrapper<O> extends InvUiWrapper<PagedGui<Item>, ConfigPagedGui, PagedGui.Builder<Item>> implements GuiPageHandler<O> {

    private final Map<String, GuiPagedActionConsumer<O>> pagedActionMap = new LinkedHashMap<>();

    public PagedInvUiWrapper(ConfigPagedGui config) {
        super(config);

        this.setPageActions(this);
    }

    @Override
    protected PagedGui<Item> guiFromBuilder(PagedGui.Builder<Item> builder) {
        return builder.build();
    }

    @Override
    protected PagedGui.Builder<Item> createBuilder() {
        return PagedGui.items();
    }

    @Override
    protected void additionalData(PagedGui.Builder<Item> builder) {
        this.config.getPagedItem().ifPresent(item ->
                builder.addIngredient(item.getShapeCharacter(), Markers.CONTENT_LIST_SLOT_HORIZONTAL));
        builder.setContent(this.loadContents());
    }

    @Override
    public void refresh() {
        this.gui.setContent(loadContents());
        this.gui.setPage(0);
        super.refresh();
    }

    @Override
    public int maxPages() {
        return this.gui.getPageAmount();
    }

    @Override
    public int currentPage() {
        return this.gui.getCurrentPage() + 1;
    }

    @Override
    public void nextPage() {
        this.gui.goForward();
    }

    @Override
    public void previousPage() {
        this.gui.goBack();
    }

    @Override
    public Map<String, GuiPagedActionConsumer<O>> getPagedActionMap() {
        return this.pagedActionMap;
    }

    private List<Item> loadContents() {
        return this.config.getPagedItem().map(pagedItem -> {
            List<GuiPagedObject<O>> objects = this.loadPageObjects();

            List<Item> contents = new ArrayList<>(objects.size());

            this.listPageObjects((replacers, clickConsumer) -> {
                InvUiItem item = new InvUiItem(this, pagedItem);
                item.setCustomReplacers(replacers);
                item.setDefaultConsumer(clickConsumer);
                contents.add(item);
            });

            return contents;
        }).orElse(List.of());
    }
}
