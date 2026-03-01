package fr.ibrakash.helper.configuration.objects.action;

import org.bukkit.event.inventory.ClickType;

import java.util.*;
import java.util.function.Consumer;

public class ConfigGroupAction {

    private List<ConfigAction> actions = new ArrayList<>(List.of(
            new ConfigAction()
    ));
    private final Map<ClickType, List<ConfigAction>> clickActionMap = new EnumMap<>(ClickType.class);

    public ConfigGroupAction() {
        this.setup();
    }

    public ConfigGroupAction actions(List<ConfigAction> actions) {
        this.actions = new ArrayList<>(actions);
        this.setup();
        return this;
    }

    public void setup() {
        this.clickActionMap.clear();
        this.actions.forEach(action ->
                action.getClickTypes().forEach(configClick ->
                        configClick.getTypes().forEach(type ->
                                this.clickActionMap.computeIfAbsent(type, t -> new ArrayList<>()).add(action))));
    }

    public void runActions(ClickType clickType, Consumer<String> consumer) {
        List<ConfigAction> list = this.clickActionMap.get(clickType);
        if (list == null) return;
        list.forEach(configAction -> configAction.getExecute().forEach(consumer));
    }

    public List<ConfigAction> getActions() {
        return actions;
    }
}
