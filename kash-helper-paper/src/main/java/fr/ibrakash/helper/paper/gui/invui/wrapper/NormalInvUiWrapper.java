package fr.ibrakash.helper.paper.gui.invui.wrapper;

import fr.ibrakash.helper.paper.configuration.objects.gui.ConfigGui;
import fr.ibrakash.helper.paper.gui.invui.InvUiWrapper;
import xyz.xenondevs.invui.gui.Gui;

public abstract class NormalInvUiWrapper extends InvUiWrapper<Gui, ConfigGui, Gui.Builder.Normal> {

    public NormalInvUiWrapper(ConfigGui config) {
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
