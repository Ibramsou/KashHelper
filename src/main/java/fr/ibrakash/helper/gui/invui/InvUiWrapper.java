package fr.ibrakash.helper.gui.invui;

import fr.ibrakash.helper.configuration.objects.AbstractGuiConfig;
import fr.ibrakash.helper.gui.GuiWrapper;
import org.bukkit.entity.Player;
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public abstract class InvUiWrapper<G extends Gui, C extends AbstractGuiConfig, B extends Gui.Builder<?, ?>> extends GuiWrapper<G, C, Window> {

    private final List<InvUiItem> items = new ArrayList<>();
    private boolean closedByPlayer = true;

    public InvUiWrapper(C config) {
        super(config);
    }

    @Override
    public G build() {
        B builder = this.createBuilder();
        builder.setStructure(this.config.getShape().toArray(String[]::new));

        this.config.getItems().values().forEach(configGuiItem -> {
            InvUiItem item = new InvUiItem(this, configGuiItem, configItem ->
                    configItem.build(this.textReplacer, this.itemReplacer));
            builder.addIngredient(configGuiItem.getShapeCharacter(), item);
            this.items.add(item);
        });

        this.additionalData(builder);

        return guiFromBuilder(builder);
    }

    @Override
    protected Window createWindow(Player player) {
        Window window = Window.single()
                .setViewer(player)
                .setGui(this.gui)
                .setTitle(new AdventureComponentWrapper(this.title()))
                .build();
        window.addCloseHandler(() -> this.handleClose(this.closedByPlayer));
        window.open();
        return window;
    }

    @Override
    public void refresh() {
        if (this.window == null) return;
        this.window.changeTitle(new AdventureComponentWrapper(this.title()));
        this.items.forEach(InvUiItem::updateItem);
    }

    @Override
    public void close() {
        this.closedByPlayer = false;
        this.window.close();
        this.closedByPlayer = true;
    }

    protected void additionalData(B builder) {}

    protected abstract G guiFromBuilder(B builder);

    protected abstract B createBuilder();
}
