package fr.ibrakash.helper.example.gui;

import fr.ibrakash.helper.example.ExampleGuiConfig;
import fr.ibrakash.helper.gui.invui.wrapper.NormalInvUiWrapper;
import fr.ibrakash.helper.utils.TextUtil;

import java.util.List;

public class ExampleGui extends NormalInvUiWrapper {

    public ExampleGui() {
        super(ExampleGuiConfig.get().getNormalGui());

        this.setAction("click_me", (issuer, type, event, item) ->
                issuer.sendMessage(TextUtil.replacedComponent("<red>Why did you click?"))
        );
    }

    @Override
    public List<Object> getReplacers() {
        return List.of();
    }
}
