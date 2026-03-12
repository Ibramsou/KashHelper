package fr.ibrakash.helper.material;

import org.bukkit.Material;

import java.util.*;
import java.util.function.Supplier;

public class MaterialUtil {

    private static final Map<String, Set<Material>> toolsMaterials = new HashMap<>();
    private static final Map<MaterialType, Set<Material>> materialTypes = new EnumMap<>(MaterialType.class);

    static {
        for (Material value : Material.values()) {
            String name = value.name();
            if (name.startsWith("LEGACY_")) continue;

            for (MaterialType materialType : MaterialType.values()) {
                for (String s : materialType.getContent()) {
                    if (name.contains(s)) {
                        materialTypes.computeIfAbsent(materialType,
                                type -> EnumSet.noneOf(Material.class))
                                .add(value);
                    }
                }

            }

            if (name.contains("SWORD")) {
                registerTool("sword", value);
            } else if (name.contains("PICKAXE")) {
                registerTool("pickaxe", value);
            } else if (name.contains("AXE")) {
                registerTool("axe", value);
            } else if (name.contains("HOE")) {
                registerTool("hoe", value);
            }
        }
    }

    public static Set<Material> getMaterials(MaterialType type) {
        return materialTypes.getOrDefault(type, Collections.emptySet());
    }

    public static boolean isType(Material material, MaterialType materialType) {
        return materialTypes.get(materialType).contains(material);
    }

    private static void registerTool(String identifier, Material material) {
        toolsMaterials.computeIfAbsent(identifier, s -> EnumSet.noneOf(Material.class)).add(material);
    }

    public static Set<Material> toItemWhitelist(List<String> items) {
        Set<Material> itemWhitelist = new HashSet<>();
        for (String input : items) {
            Set<Material> materials = MaterialUtil.getTools(input);
            if (materials != null) {
                itemWhitelist.addAll(materials);
                continue;
            }

            Material material = Material.getMaterial(input);
            if (material == null) continue;
            itemWhitelist.add(material);
        }
        return itemWhitelist;
    }

    public static Set<Material> getTools(String identifier) {
        return toolsMaterials.get(identifier.toLowerCase());
    }

    public static Material parse(String name) {
        return parseOrElse(name, () -> Material.STONE);
    }

    public static Material parseOrElse(String name, Supplier<Material> defaultMaterial) {
        try {
            return Material.getMaterial(name);
        } catch (Exception exception) {
            return defaultMaterial.get();
        }
    }

    public static Material parseOrThrow(String name) {
        return parseOrThrow(name, "%s isn't a valid material name");
    }

    public static Material parseOrThrow(String name, String errorFormat) {
        return parseOrElse(name, () -> {
            throw new IllegalArgumentException(String.format(errorFormat, name));
        });
    }


    public static Material parseOrWarn(String name) {
        return parseOrWarn(name, "%s isn't a valid material name");
    }

    public static Material parseOrWarn(String name, String errorFormat) {
        return parseOrElse(name, () -> {
            System.out.println(errorFormat);
            return Material.PAPER;
        });
    }
}
