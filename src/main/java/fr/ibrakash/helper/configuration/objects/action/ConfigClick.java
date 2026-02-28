package fr.ibrakash.helper.configuration.objects.action;

import org.bukkit.event.inventory.ClickType;

import java.util.Arrays;
import java.util.List;

public enum ConfigClick {
    ALL(Arrays.asList(ClickType.values())),
    ALL_SHIFT(ClickType.SHIFT_LEFT, ClickType.SHIFT_RIGHT),
    ALL_LEFT(ClickType.LEFT, ClickType.SHIFT_LEFT),
    ALL_RIGHT(ClickType.SHIFT_RIGHT, ClickType.SHIFT_RIGHT),
    SHIFT_LEFT(ClickType.SHIFT_LEFT),
    SHIFT_RIGHT(ClickType.SHIFT_RIGHT),
    RIGHT(ClickType.RIGHT),
    LEFT(ClickType.LEFT);

    private final List<ClickType> types;

    ConfigClick(ClickType... types) {
        this(Arrays.asList(types));
    }

    ConfigClick(List<ClickType> types) {
        this.types = types;
    }

    public List<ClickType> getTypes() {
        return types;
    }
}
