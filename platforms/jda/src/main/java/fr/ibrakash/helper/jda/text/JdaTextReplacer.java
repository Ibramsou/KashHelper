package fr.ibrakash.helper.jda.text;

import fr.ibrakash.helper.platform.KashPlatform;
import fr.ibrakash.helper.platform.KashPlatformType;
import fr.ibrakash.helper.text.TextReplacer;

import java.util.List;
import java.util.function.Supplier;

public class JdaTextReplacer extends TextReplacer {

    public static JdaTextReplacer create() {
        return (JdaTextReplacer) KashPlatform.get(KashPlatformType.JDA).createTextReplacer();
    }

    public static JdaTextReplacer of(TextReplacer replacer) {
        return create().merge(replacer);
    }

    @Override
    public JdaTextReplacer add(String placeholder, Object value) {
        super.add(placeholder, value);
        return this;
    }

    @Override
    public JdaTextReplacer add(String placeholder, Supplier<Object> value) {
        super.add(placeholder, value);
        return this;
    }

    @Override
    public JdaTextReplacer merge(TextReplacer from) {
        super.merge(from);
        return this;
    }

    @Override
    public Object deserialize(String input) {
        return this.apply(input);
    }

    @Override
    public List<?> deserializeComponents(List<String> input) {
        return this.applyList(input);
    }

    @Override
    public Object deserializeItemName(String input) {
        return this.apply(input);
    }

    @Override
    public List<?> deserializeItemLore(List<String> input) {
        return this.applyList(input);
    }
}

