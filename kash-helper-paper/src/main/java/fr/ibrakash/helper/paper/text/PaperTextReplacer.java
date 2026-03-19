package fr.ibrakash.helper.paper.text;

import fr.ibrakash.helper.platform.KashPlatform;
import fr.ibrakash.helper.platform.KashPlatformType;
import fr.ibrakash.helper.text.TextReplacer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class PaperTextReplacer extends TextReplacer {

    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    public static PaperTextReplacer create() {
        return (PaperTextReplacer) KashPlatform.get(KashPlatformType.PAPER).createTextReplacer();
    }

    public static PaperTextReplacer of(TextReplacer replacer) {
        return create().merge(replacer);
    }

    @Override
    public PaperTextReplacer add(String placeholder, Object value) {
        super.add(placeholder, value);
        return this;
    }

    @Override
    public PaperTextReplacer add(String placeholder, Supplier<Object> value) {
        super.add(placeholder, value);
        return this;
    }

    @Override
    public PaperTextReplacer merge(TextReplacer from) {
        super.merge(from);
        return this;
    }

    @Override
    public Component deserialize(String input) {
        return MINI_MESSAGE.deserialize(this.apply(input));
    }

    @Override
    public List<Component> deserializeComponents(List<String> input) {
        return this.deserialize(input, MINI_MESSAGE::deserialize);
    }

    @Override
    public Component deserializeItemName(String input) {
        return this.deserialize("<italic:false>" + input);
    }

    @Override
    public List<Component> deserializeItemLore(List<String> input) {
        List<Component> result = new ArrayList<>();
        input.forEach(s -> {
            String[] lines = this.apply(s).split("\\n");
            for (String line : lines) {
                result.add(MINI_MESSAGE.deserialize("<italic:false>" + line));
            }
        });

        return result;
    }
}

