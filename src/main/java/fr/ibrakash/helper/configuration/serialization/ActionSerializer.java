package fr.ibrakash.helper.configuration.serialization;

import fr.ibrakash.helper.configuration.objects.action.ConfigAction;
import fr.ibrakash.helper.configuration.objects.action.ConfigClick;
import fr.ibrakash.helper.configuration.objects.action.ConfigGroupAction;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.serialize.SerializationException;
import org.spongepowered.configurate.serialize.TypeSerializer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.lang.reflect.Type;
import java.util.List;

@ParametersAreNonnullByDefault
public class ActionSerializer implements TypeSerializer<ConfigGroupAction> {

    private static final ActionSerializer INSTANCE = new ActionSerializer();

    public static ActionSerializer get() {
        return INSTANCE;
    }

    @Override
    public ConfigGroupAction deserialize(Type type, ConfigurationNode node) throws SerializationException {
        if (!node.isList()) {
            throw new SerializationException("Expected a list for ConfigGroupAction, got: " + node.raw());
        }

        var children = node.childrenList();
        if (children.isEmpty()) {
            return new ConfigGroupAction().actions(List.of());
        }

        boolean allMaps = children.stream().allMatch(ConfigurationNode::isMap);
        boolean allStrings = children.stream().allMatch(n -> n.raw() instanceof String);

        if (allMaps) {
            List<ConfigAction> actions = node.getList(ConfigAction.class);
            return new ConfigGroupAction().actions(actions);
        }

        if (allStrings) {
            List<String> input = node.getList(String.class);
            return new ConfigGroupAction()
                    .actions(List.of(new ConfigAction().execute(input)));
        }

        throw new SerializationException(
                "List must be either all action objects (maps) or all strings. Got: " + node.raw()
        );
    }

    @Override
    public void serialize(Type type, @Nullable ConfigGroupAction obj, ConfigurationNode node) throws SerializationException {
        if (obj == null || obj.getActions().isEmpty()) {
            node.setList(String.class, List.of());
            return;
        }
        if (obj.getActions().size() == 1 && obj.getActions().getFirst().getClickTypes().contains(ConfigClick.ALL)) {
            node.setList(String.class, obj.getActions().getFirst().getExecute());
            return;
        }

        node.setList(ConfigAction.class, obj.getActions());
    }
}
