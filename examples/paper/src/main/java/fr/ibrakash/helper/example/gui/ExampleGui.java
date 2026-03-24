package fr.ibrakash.helper.example.gui;

import fr.ibrakash.helper.example.ExamplePlugin;
import fr.ibrakash.helper.paper.gui.menu.NormalGui;
import fr.ibrakash.helper.paper.text.PaperTextUtil;

public class ExampleGui extends NormalGui {

    public ExampleGui(ExamplePlugin plugin) {
        super(plugin.getExampleMenus().getNormal("normal-gui"));

        this.action("click_me", (issuer, type, event, item) ->
                issuer.sendMessage(PaperTextUtil.get().replacedComponent("<red>Why did you click?"))
        );
    }

    @Override
    public void handleClose(boolean byPlayer) {}
}
