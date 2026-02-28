package fr.ibrakash.helper.stream;

import fr.ibrakash.helper.configuration.objects.stream.ConfigFilter;
import fr.ibrakash.helper.configuration.objects.stream.ConfigFilterMode;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class StreamFilterUtil {

    public static String getModesInfo(StreamFilter<?> streamFilter, String filterName) {
        ConfigFilter filter = streamFilter.config.getFilters().get(filterName);
        if (filter == null) return "none";
        String selected = filter.getSelectedEntry();
        String unselected = filter.getUnSelectedEntry();
        ConfigFilterMode currentMode = streamFilter.filterModes.getOrDefault(filterName, filter.getModes().getFirst());
        return StringUtils.join(filter.getModes().stream().map(mode -> currentMode == mode ?
                        selected.replace("%mode%", mode.getText()) : unselected.replace("%mode%", mode.getText()))
                .toList(), "\n<italic:false>");
    }

    public static void changeFilterMode(StreamFilter<?> streamFilter, String filterName) {
        changeFilterMode(streamFilter, filterName, true);
    }

    public static void changeFilterMode(StreamFilter<?> streamFilter, String filterName, boolean forward) {
        ConfigFilter filter = streamFilter.config.getFilters().get(filterName);
        if (filter == null) return;
        List<ConfigFilterMode> modes = filter.getModes();
        if (modes.isEmpty()) throw new UnsupportedOperationException("Filter mode " + filterName + " is empty");
        ConfigFilterMode currentMode = streamFilter.filterModes.getOrDefault(filter.getFilterId(), modes.getFirst());
        int indexOf = modes.indexOf(currentMode);
        ConfigFilterMode newMode;
        if (forward) {
            if (indexOf + 1 >= modes.size()) {
                newMode = modes.getFirst();
            } else {
                newMode = modes.get(indexOf + 1);
            }
        } else {
            if (indexOf - 1 < 0) {
                newMode = modes.getLast();
            } else {
                newMode = modes.get(indexOf - 1);
            }
        }

        streamFilter.filterModes.put(filter.getFilterId(), newMode);
    }
}
