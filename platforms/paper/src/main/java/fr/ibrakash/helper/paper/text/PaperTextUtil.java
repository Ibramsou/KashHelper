package fr.ibrakash.helper.paper.text;

import fr.ibrakash.helper.platform.KashPlatform;
import fr.ibrakash.helper.platform.KashPlatformType;
import fr.ibrakash.helper.text.TextUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class PaperTextUtil extends TextUtil<Component> {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static PaperTextUtil get() {
        return (PaperTextUtil) KashPlatform.get(KashPlatformType.PAPER).textUtil();
    }

    @Override
    public Component replacedComponent(String input, List<Object> replacers) {
        return MINI_MESSAGE.deserialize(TextUtil.replaced(input, replacers));
    }

    @Override
    public List<Component> replacedComponents(List<String> input, List<Object> replacers, UnaryOperator<String> operator) {
        List<Component> results = new ArrayList<>();
        input.forEach(s -> results.add(this.replacedComponent(operator == null ? s : operator.apply(s), replacers)));
        return results;
    }

    @Override
    public Component replacedItemName(String input, List<Object> replacers) {
        return this.replacedComponent("<italic:false>" + input, replacers);
    }

    @Override
    public List<Component> replacedItemLore(List<String> input, List<Object> replacers) {
        List<Component> results = new ArrayList<>();
        input.forEach(s -> {
            String[] lines = TextUtil.replaced(s, replacers).split("\\n");
            for (String line : lines) {
                results.add(MINI_MESSAGE.deserialize("<italic:false>" + line));
            }
        });
        return results;
    }

    @Override
    public Component replacedComponent(String input, Object... replacers) {
        return super.replacedComponent(input, replacers);
    }

    @Override
    public Component replacedItemName(String input, Object... replacers) {
        return super.replacedItemName(input, replacers);
    }

    @Override
    public List<Component> replacedItemLore(List<String> input, Object... replacers) {
        return super.replacedItemLore(input, replacers);
    }
}
