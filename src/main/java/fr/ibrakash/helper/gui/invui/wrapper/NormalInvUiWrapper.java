package fr.ibrakash.helper.gui.invui.wrapper;

import fr.ibrakash.helper.configuration.objects.gui.GuiConfig;
import fr.ibrakash.helper.gui.invui.InvUiWrapper;
import xyz.xenondevs.invui.gui.Gui;

public abstract class NormalInvUiWrapper extends InvUiWrapper<Gui, GuiConfig, Gui.Builder.Normal> {

    public NormalInvUiWrapper(GuiConfig config) {
        super(config);
    }

    @Override
    protected Gui guiFromBuilder(Gui.Builder.Normal builder) {
        return builder.build();
    }

    @Override
    protected Gui.Builder.Normal createBuilder() {
        return Gui.normal();
    }
}
