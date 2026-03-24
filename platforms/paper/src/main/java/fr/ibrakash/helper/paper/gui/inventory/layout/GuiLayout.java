package fr.ibrakash.helper.paper.gui.inventory.layout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record GuiLayout(int rows, Map<Character, List<Integer>> slotsByCharacter) {

    public static GuiLayout fromShape(List<String> shape) {
        int rows = Math.max(1, Math.min(6, shape.size()));
        Map<Character, List<Integer>> map = new HashMap<>();

        for (int row = 0; row < rows; row++) {
            String line = row < shape.size() ? shape.get(row) : "";
            String normalized = normalizeLine(line);

            for (int column = 0; column < 9; column++) {
                char ingredient = column < normalized.length() ? normalized.charAt(column) : ' ';
                map.computeIfAbsent(ingredient, key -> new ArrayList<>())
                        .add((row * 9) + column);
            }
        }

        return new GuiLayout(rows, map);
    }

    public List<Integer> slots(char ingredient) {
        return this.slotsByCharacter.getOrDefault(ingredient, List.of());
    }

    public int size() {
        return this.rows * 9;
    }

    private static String normalizeLine(String line) {
        if (line == null) {
            return "";
        }
        return line.replace(" ", "").replace("\t", "");
    }
}

