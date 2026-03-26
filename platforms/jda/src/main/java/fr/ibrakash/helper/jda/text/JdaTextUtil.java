package fr.ibrakash.helper.jda.text;

import fr.ibrakash.helper.platform.KashPlatform;
import fr.ibrakash.helper.platform.KashPlatformType;
import fr.ibrakash.helper.text.TextUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class JdaTextUtil extends TextUtil<String> {

    public static JdaTextUtil get() {
        return (JdaTextUtil) KashPlatform.get(KashPlatformType.JDA).textUtil();
    }

    @Override
    public String replacedComponent(String input, List<Object> replacers) {
        return TextUtil.replaced(input, replacers);
    }

    @Override
    public List<String> replacedComponents(List<String> input, List<Object> replacers, UnaryOperator<String> operator) {
        List<String> results = new ArrayList<>();
        input.forEach(s -> results.add(this.replacedComponent(operator == null ? s : operator.apply(s), replacers)));
        return results;
    }

    @Override
    protected String replacedItemName(String input, List<Object> replacers) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected List<String> replacedItemLore(List<String> input, List<Object> replacers) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}

